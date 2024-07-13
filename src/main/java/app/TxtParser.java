package app;

import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import org.herac.tuxguitar.song.factory.TGFactory;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.*;
import org.herac.tuxguitar.io.base.TGSongWriterHandle;
import org.herac.tuxguitar.io.gtp.GP5OutputStream;
import org.herac.tuxguitar.io.gtp.GTPSettings;

public class TxtParser
{
    Integer lastTime, time;
    // Inner unit of time when parsing text
    int ticksForQuarter = 2;
    int ticksForMeasure = ticksForQuarter*4;    // 4/4 measure

    int string;
    TGFactory factory;
    TGSong song;
    TGTrack track;
    TGSongManager manager;
    Tab tab;

    public TGSong getSong()
    {
        return song;
    }

    // Create a new measure if ticks is in a new measure
    protected void addMeasuresIfNeeded(int ticks)
    {
        int measureNum = ticks/ticksForMeasure;

        while (track.getMeasure(measureNum) == null)
            manager.addNewMeasureBeforeEnd(song);
    }

    // e.g. 5/8 = 1/2 + 1/8
    // 3/8 ok = 1/4 dotted = 3/8
    // 7/8 = 1/2dotted + 1/8
    protected int getMeasureDivisorForSingleNote(int ticks)
    {
        int divisor;
        for (divisor=1; divisor<16; divisor*=2)
        {
            if (ticksForMeasure/divisor <= ticks)
                break;
        }
        return divisor;
    }

    protected boolean shouldNoteBeDotted(int divisor, int ticks)
    {
        int undottedTicks = ticksForMeasure/divisor;
        int dottedTicks = 3*undottedTicks/2;
        // These should only coincide if divisor > ticksForMeasure
        // in that case, not just a dot, but the note does not make sense really

        return dottedTicks <= ticks && dottedTicks > undottedTicks;
    }

    protected int getTicksForSingleNote(int divisor, boolean dotted)
    {
        int undottedTicks = ticksForMeasure/divisor;
        int dottedTicks = 3*undottedTicks/2;

        if (dotted)
            return dottedTicks;
        return undottedTicks;
    }

    protected void addRest(int measureNum, int ticks)
    {
        TGMeasure measure = track.getMeasure(measureNum);

        lastTime += ticks;

        while (ticks > 0)
        {
            int divisor = getMeasureDivisorForSingleNote(ticks);
            boolean dotted = shouldNoteBeDotted(divisor, ticks);
            int usingTicks = getTicksForSingleNote(divisor, dotted);

            TGBeat beat = factory.newBeat();
            TGVoice voice = beat.getVoice(0);
            // this is how a pause is kept
            voice.setEmpty(false);

            TGDuration duration = factory.newDuration();
            duration.setValue(divisor);
            duration.setDotted(dotted);
            voice.setDuration(duration);
            measure.addBeat(beat);

            ticks -= usingTicks;
        }
    }

    protected void addStroke(int ticksStroke, Stroke stroke)
    {
        addMeasuresIfNeeded(ticksStroke);
        // lastTime cannot be before the beginning of the measure
        lastTime = Math.max(lastTime, (ticksStroke/ticksForMeasure)*ticksForMeasure);

        int measureNum = ticksStroke/ticksForMeasure;
        addRest(measureNum, ticksStroke - lastTime);
        TGMeasure measure = track.getMeasure(measureNum);

        int durStroke = ticksForMeasure - (ticksStroke % ticksForMeasure);
        Integer ticksNextStroke = tab.getNextTime(ticksStroke);
        if (ticksNextStroke != null)
            // Avoid cutting into the next measure
            durStroke = Math.min(durStroke, ticksNextStroke - ticksStroke);
        // Prefer even duration if >1/4, so 3/8 -> 1/4, 3/4 -> 1/2
        if (durStroke > ticksForQuarter && (durStroke % 2) == 1)
            durStroke--;
        lastTime += durStroke;

        while (durStroke > 0)
        {
            int divisor = getMeasureDivisorForSingleNote(durStroke);
            boolean dotted = shouldNoteBeDotted(divisor, durStroke);
            int usingTicks = getTicksForSingleNote(divisor, dotted);

            TGBeat beat = factory.newBeat();
            TGVoice voice = beat.getVoice(0);

            TGDuration duration = factory.newDuration();
            System.out.printf("Voice measure: %d, time: %d, division: %d, dotted: %b, ticks: %d\n", measureNum, time, divisor, dotted, usingTicks);
            duration.setValue(divisor);
            duration.setDotted(dotted);
            voice.setDuration(duration);

            for (Map.Entry<Integer, Integer> entry : stroke.getNotes().entrySet())
            {
                int string = entry.getKey();
                int fret = entry.getValue();

                System.out.printf("  note string: %d, fret: %d\n", string, fret);
                TGNote note = factory.newNote();
                note.setValue(fret);
                note.setString(string);
                voice.addNote(note);
            }
            measure.addBeat(beat);

            durStroke -= usingTicks;
            time += usingTicks;
        }
    }

    protected void init()
    {
        time = 0;
        lastTime = 0;
        string = 0;
        tab = new Tab();

        manager = new TGSongManager();
        factory = manager.getFactory();
        song = manager.newSong();
        song.setComments("created with tab2gp");
        track = song.getTrack(0);
    }

    protected void createTGSong()
    {
        lastTime = 0;
        time = 0;
        while (true)
        {
            time = tab.getNextTime(lastTime-1);
            if (time == null)
                break;
            Stroke stroke = tab.getStrokeAt(time);
            addStroke(time, stroke);
        }
    }

    public void saveAsGP(String outFileName) throws FileNotFoundException
    {
        GTPSettings settings = new GTPSettings();
        GP5OutputStream writer = new GP5OutputStream(settings);
        TGSongWriterHandle handle = new TGSongWriterHandle();
        FileOutputStream outputStream = new FileOutputStream(outFileName);
        handle.setOutputStream(outputStream);
        handle.setSong(song);
        handle.setFactory(factory);
        writer.write(handle);
    }

    // TODO: Add clean line to remove html tags as <p /> before tab line
    protected boolean parseLine(String line)
    {
        // Remove html tags if any
        line = line.replaceAll("<[^>]*>", "");

        // Expect ticks to be multiple of a measure and at least one
        // TODO: Consider regex to exclude invalid lines, expect only [0-9- ]
        if ((line.length() % (2*ticksForMeasure)) != 2 ||
            line.length() < (2+2*ticksForMeasure) ||
            line.charAt(1) != '|')
        {
            // Flush
            lastTime = time;
            string = 0;

            return false;
        }

        time = lastTime;
        Character stringName = line.charAt(0);
        // TODO: Consider using stringName for checks

        string++;

        for (int i=2; i<(line.length()-1); i+=2, time++)
        {
            Character c1 = line.charAt(i);
            Character c2 = line.charAt(i+1);
            if (c2 == '-' || c2 == ' ')
                continue;
            int fret = 0;
            if (c2 < '0' || c2 > '9') {
                // At least a measure is needed
                if (i <= (2 + 2*ticksForMeasure))
                    return false;
                break;
            }

            fret = c2 - '0';
            if (c1 >= '0' && c1 <= '9')
                fret += 10*(c1 - '0');
            System.out.printf("Detected note, tick: %d, string: %d, fret: %d\n", time, string, fret);
            tab.addNote(time, string, fret);
        }

        System.out.println(line);

        return true;
    }

    public TxtParser(Scanner scanner)
    {
        // TODO: Consider cli options to set ticksForMeasure and ticksForQuarter
        init();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            parseLine(line);
            // TODO: Add warn log if returns false
        }

        createTGSong();
    }
}

