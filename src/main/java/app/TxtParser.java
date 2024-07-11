package app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;   // To read text files

import org.herac.tuxguitar.song.factory.TGFactory;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.*;
import org.herac.tuxguitar.io.base.TGSongPersistenceHandle;
import org.herac.tuxguitar.io.base.TGSongWriterHandle;
import org.herac.tuxguitar.io.gtp.GP5OutputStream;
import org.herac.tuxguitar.io.gtp.GTPSettings;
import org.herac.tuxguitar.song.managers.TGMeasureManager;


// common/TuxGuitar-lib/src/org/herac/tuxguitar/song/managers/TGSongManager.java
// common/TuxGuitar-lib/src/org/herac/tuxguitar/song/managers/TGMeasureManager.java
// common/TuxGuitar-gtp/src/org/herac/tuxguitar/io/gtp/GP3InputStream.java
// common/TuxGuitar-gtp/src/org/herac/tuxguitar/io/gtp/GP5OutputStream.java

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

    // Create a new measure if timeNote is part of a new measure
    protected void addMeasuresIfNeeded(int timeNote)
    {
        int measureNum = timeNote/ticksForMeasure;

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
        // This 2 should only coincide if divisor > ticksForMeasure
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

        ticks = ticks % ticksForMeasure;

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
            duration.setValue(divisor); // See TGDuration.QUARTER
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

        Integer ticksNextStroke = tab.getNextTime(ticksStroke);
        int durStroke = ticksForMeasure - (ticksStroke % ticksForMeasure);
        if (ticksNextStroke != null)
            durStroke = ticksNextStroke - ticksStroke;
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
            duration.setValue(divisor); // See TGDuration.QUARTER
            duration.setDotted(dotted);
            voice.setDuration(duration);

            for (Map.Entry<Integer, Integer> entry : stroke.getNotes().entrySet())
            {
                int string = entry.getKey();
                int fret = entry.getValue();

System.out.printf("  note string %d, fret: %d\n", string, fret);
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

    // TODO: cleanup
    protected void init()
    {
        tab = new Tab();
        time = 0;
        lastTime = 0;
        string = 0;

        //TGSong song = new TGSong("song1");
        //factory = new TGFactory();
        //new TGSongReaderHandle();
        manager = new TGSongManager();
        //TGSongPersistenceHandle handle = new TGSongReaderHandle();
        //factory = handle.getFactory();
        //System.out.println("dbg2");
        factory = manager.getFactory();
        song = manager.newSong();
        song.setComments("created by Mauro");

        int measureNum = 1;
        manager.addNewMeasure(song, measureNum);
        measureNum = 2;
        manager.addNewMeasure(song, measureNum);


        track = song.getTrack(0);
        TGMeasure measure = track.getMeasure(0);

        // this is ignored as empty
        TGBeat beat = factory.newBeat();
        TGVoice voice = beat.getVoice(0);
        TGNote note = factory.newNote();
        voice.addNote(note);
        measure.addBeat(beat);

        // pause ??
        beat = factory.newBeat();
        voice = beat.getVoice(0);
        // this is how a pause is kept
        voice.setEmpty(false);
        TGDuration duration = factory.newDuration();
        duration.setValue(TGDuration.QUARTER);
        duration.setDotted(true);
        voice.setDuration(duration);
        measure.addBeat(beat);

        beat = factory.newBeat();
        //beat.setStart(960*2/4);
        //beat.getStroke().setValue(4);
        voice = beat.getVoice(0);
        duration = factory.newDuration();
        duration.setValue(TGDuration.EIGHTH);
        voice.setDuration(duration);

        note = factory.newNote();
        note.setValue(3);
        note.setString(1);
        voice.addNote(note);

        note = factory.newNote();
        note.setValue(4);
        note.setString(2);

        voice.addNote(note);

        measure.addBeat(beat);
    }

    // TODO: remove
    protected void insertNotes(int time)
    {
        Integer nextTime = tab.getNextTime(time);
        int dur = 8 - (time % 8);
        if (nextTime != null)
            dur = nextTime - time;

        int measureNum = time/8;
        //int lastMeasureNum = 1+ lastTime/8;
        Stroke stroke = tab.getStrokeAt(time);

        TGBeat beat = factory.newBeat();
        beat.setStart(TGDuration.QUARTER_TIME/2*(time%8));
        TGVoice voice = beat.getVoice(0);

        TGDuration duration = factory.newDuration();

        int rest = 0;
        if (dur == 1)
            duration.setValue(TGDuration.EIGHTH);
        else if (dur >= 2 && dur < 4)
        {
            duration.setValue(TGDuration.QUARTER);
            rest = dur - 2;
        }
        else
        {
            duration.setValue(TGDuration.HALF);
            rest = dur - 4;
        }

        voice.setDuration(duration);


        // TODO: add pause before
        for (Map.Entry<Integer, Integer> entry : stroke.getNotes().entrySet())
        {
            int string = entry.getKey();
            int fret = entry.getValue();

System.out.printf("Add time %d, measure %d, string %d, fret: %d\n", time, measureNum, string, fret);
            TGNote note = factory.newNote();
            note.setValue(fret);
            note.setString(string);
            voice.addNote(note);
        }
        //while (track.countMeasures() <= measureNum)
        //
        //while (manager.getMeasures(song, measureNum).isEmpty())
        //    manager.addNewMeasureBeforeEnd(song);

        while (track.getMeasure(measureNum) == null)
            manager.addNewMeasureBeforeEnd(song);
        //    manager.addNewMeasure(song, measureNum);

        //if (manager.getMeasures(song, measureNum).isEmpty())
        //    manager.addNewMeasure(song, measureNum);
        TGMeasure measure = track.getMeasure(measureNum);

        TGMeasureManager measureManager = manager.getMeasureManager();
        //if (lastMeasureNum >= 0 && measureNum != lastMeasureNum)
        //    measureManager.autoCompleteSilences(track.getMeasure(lastMeasureNum));

        //for (int i=manager.getMeasures(song, measureNum)

            //manager.addNewMeasure(song, track.countMeasures());

        //TGMeasure measure = track.getMeasure(measureNum);
        //TGMeasure measure = manager.getMeasures(song, measureNum).get(0);
        measure.addBeat(beat);

        lastTime = time;

        if (rest > 0)
        {
            /*
            duration = factory.newDuration();
            beat = factory.newBeat();
            voice = beat.getVoice(0);
            measureManager.createSilences(measureNum, time + dur - rest, rest, 0);
            */


            /*
            if (rest == 1)
                duration.setValue(TGDuration.EIGHTH);
            else if (rest >= 2 && rest < 4)
            {
                duration.setValue(TGDuration.QUARTER);
            }
            else
            {
                duration.setValue(TGDuration.HALF);
            }
            voice.setDuration(duration);
            */
        }

    }
    // see addNote
    // common/TuxGuitar-gtp/src/org/herac/tuxguitar/io/gtp/GP5InputStream.java

    // TODO: extract out
    protected void write(String outFileName) throws FileNotFoundException
    {
        /*
        lastTime = -1;
        time = -1;
        while (true)
        {
            time = tab.getNextTime(time);
            if (time == null || time <= lastTime)
                break;
            insertNotes(time);
            lastTime = time;
        }
        */

        lastTime = 0;
        time = 0;
        while (true)
        {
            //time = tab.getNextTime(time-1);
            time = tab.getNextTime(lastTime-1);
            if (time == null)
                break;
            Stroke stroke = tab.getStrokeAt(time);
            addStroke(time, stroke);
            //lastTime = time;
            //time = lastTime;
        }
        //int ticksLastRest = ticksForMeasure - (lastTime % ticksForMeasure);
        //addRest(lastTime / ticksForMeasure, ticksLastRest);

        //manager.moveOutOfBoundsBeatsToNewMeasure(song, 0);

        GTPSettings settings = new GTPSettings();
        GP5OutputStream writer = new GP5OutputStream(settings);
        TGSongWriterHandle handle = new TGSongWriterHandle();
        FileOutputStream outputStream = new FileOutputStream(outFileName);
        handle.setOutputStream(outputStream);
        handle.setSong(song);
        handle.setFactory(factory);
        writer.write(handle);
    }

    // TODO: add clean line
    protected boolean parseLine(String line)
    {
        if (line.length() < 67 || line.charAt(1) != '|')
        {
            // TODO: flush
            lastTime = time;
            string = 0;

            return false;
        }

        time = lastTime;
        Character stringName = line.charAt(0);

        string++;

        for (int i=2; i<(line.length()-1); i+=2, time++)
        {
            Character c1 = line.charAt(i);
            Character c2 = line.charAt(i+1);
            if (c2 == '-' || c2 == ' ')
                continue;
            int val = 0;
            if (c2 < '0' || c2 > '9') {
                if (i == 2)
                    return false;
                break;
            }

            val = c2 - '0';
            if (c1 >= '0' && c1 <= '9')
                val += 10*(c1 - '0');
            // add i/2 -> val
            // TODO: consider allowing a choice for time granularity
            //System.out.printf("%d %d\n", i/2, val);
            tab.addNote(time, string, val);
        }

        System.out.println(line);

        return true;
    }


    public TxtParser(String inFileName, String outFileName)
    {
        try {
            File inFile = new File(inFileName);
            Scanner scanner = new Scanner(inFile);
            init();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                parseLine(line);
            }
            scanner.close();

            write(outFileName);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

