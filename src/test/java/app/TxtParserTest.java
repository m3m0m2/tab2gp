package app;

import java.util.List;
import java.util.Scanner;

import org.herac.tuxguitar.song.models.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TxtParserTest
{
    private TxtParser parser;
    private String txtInput = "";
    private FakeTab fakeTab = new FakeTab();

    private void setUpInput1()
    {
        txtInput += fakeTab.toString();
        Scanner scanner = new Scanner(txtInput);
        parser = new TxtParser(scanner);
    }

    private void assertIsRest(TGVoice voice, int division, boolean dotted)
    {
        TGDuration duration = voice.getDuration();
        List<TGNote> notes = voice.getNotes();

        assertTrue(voice.isRestVoice());
        assertEquals(false, voice.isEmpty());
        assertEquals(0, notes.size());
        assertEquals(division, duration.getValue());
        assertEquals(dotted, duration.isDotted());
    }

    private void assertVoiceHasNotes(TGVoice voice, int division, boolean dotted, Stroke stroke)
    {
        TGDuration duration = voice.getDuration();
        List<TGNote> notes = voice.getNotes();

        assertFalse(voice.isRestVoice());
        assertEquals(false, voice.isEmpty());
        assertEquals(stroke.getNotes().size(), notes.size());

        for (TGNote note : notes)
        {
            Integer expectedFret = stroke.getNotes().get(note.getString());

            assertTrue(expectedFret != null);
            assertEquals(expectedFret.intValue(), note.getValue());
        }
    }

    @Test
    public void getSingleNoteAfterPause()
    {
        // X|-----------
        // X|-- 5-------
                      // tick, string, fret
        fakeTab.setNote(1, 2, 5);

        Stroke expectedStroke = new Stroke();
                        // string, fret
        expectedStroke.addNote(2, 5);

        setUpInput1();

        TGSong song = parser.getSong();
        TGTrack track = song.getTrack(0);
        TGMeasure measure = track.getMeasure(0);
        List<TGBeat> beats = measure.getBeats();

        assertEquals(2, beats.size());

        // First Beat is rest
        TGBeat beat = beats.get(0);
        TGVoice voice = beat.getVoice(0);

        assertIsRest(voice, 8, false);

        // Second Beat
        beat = beats.get(1);
        voice = beat.getVoice(0);

        assertVoiceHasNotes(voice, 2, false, expectedStroke);
    }

    @Test
    public void getMultipleNotesRepeated()
    {
        //  |-0-1-2-3-4-5-6-7|8-9 tick
        // X|----------------|
        // X|-- 5-- 5 5   5 5 5 5 outside measure

                      // tick, string, fret
        fakeTab.setNote(1, 2, 5);
        fakeTab.setNote(3, 2, 5);
        fakeTab.setNote(4, 2, 5);
        fakeTab.setNote(6, 2, 5);
        fakeTab.setNote(7, 2, 5);
        fakeTab.setNote(8, 2, 5);
        fakeTab.setNote(9, 2, 5);

        Stroke expectedStroke = new Stroke();
                        // string, fret
        expectedStroke.addNote(2, 5);

        setUpInput1();

        TGSong song = parser.getSong();
        TGTrack track = song.getTrack(0);
        TGMeasure measure = track.getMeasure(0);
        List<TGBeat> beats = measure.getBeats();

        assertEquals(6, beats.size());

        // Beat 0 tick 0 is rest
        TGBeat beat = beats.get(0);
        TGVoice voice = beat.getVoice(0);

        assertIsRest(voice, 8, false);

        // Beat 1 tick 1
        beat = beats.get(1);
        voice = beat.getVoice(0);

        assertVoiceHasNotes(voice, 4, false, expectedStroke);

        // Beat 2 tick 3
        beat = beats.get(2);
        voice = beat.getVoice(0);
        assertVoiceHasNotes(voice, 8, false, expectedStroke);

        // Beat 3 tick 4
        beat = beats.get(3);
        voice = beat.getVoice(0);
        assertVoiceHasNotes(voice, 4, false, expectedStroke);

        // Beat 4 tick 6
        beat = beats.get(4);
        voice = beat.getVoice(0);
        assertVoiceHasNotes(voice, 8, false, expectedStroke);

        // Beat 5 tick 7
        beat = beats.get(5);
        voice = beat.getVoice(0);
        assertVoiceHasNotes(voice, 8, false, expectedStroke);
    }

    @Test
    public void getSecondMeasureRight()
    {
        txtInput =
//         0       |       1       |       2       |       3       |
//  0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
"E|------------------------------------------------------ 3--------<br />\n" +
"B|---------------------------- 4-- 7------ 4-------- 4-- 4------ 4<br />\n" +
"G|------------------------ 3 5---- 6------ 3---- 5 5---- 4------ 3<br />\n" +
"D|-------------------------------- 5------ 4-------------------- 2<br />\n" +
"A|-------------------------------------------------------------- 3<br />\n" +
"E|-------------------------------- 5------ 4------------ 3--------<br />";

        Scanner scanner = new Scanner(txtInput);
        parser = new TxtParser(scanner);

        TGSong song = parser.getSong();
        TGTrack track = song.getTrack(0);
        TGMeasure measure = track.getMeasure(1);
        List<TGBeat> beats = measure.getBeats();

        Stroke expectedStroke = new Stroke();
        assertEquals(4, beats.size());

        // Beat 0 tick 0 is rest 4/2 dur
        TGBeat beat = beats.get(0);
        TGVoice voice = beat.getVoice(0);
        assertIsRest(voice, 2, false);

        // Beat 1 tick 4
        beat = beats.get(1);
        voice = beat.getVoice(0);

                        // string, fret
        expectedStroke.addNote(3, 3);
        assertVoiceHasNotes(voice, 8, false, expectedStroke);

        // Beat 2 tick 5
        beat = beats.get(2);
        voice = beat.getVoice(0);

                        // string, fret
        expectedStroke.addNote(3, 5);
        assertVoiceHasNotes(voice, 8, false, expectedStroke);

        // Beat 3 tick 6
        beat = beats.get(3);
        voice = beat.getVoice(0);

                        // string, fret
        expectedStroke.clear();
        expectedStroke.addNote(2, 4);
        assertVoiceHasNotes(voice, 4, false, expectedStroke);
    }

    @Test
    public void cleanseHtmlTagsFromLine()
    {
        String content =
"E|-3---------------------------------------------------- 3--------\n";
        txtInput = "<p /><tab1>" + content + "<br /><p />";

        Scanner scanner = new Scanner(txtInput);
        parser = new TxtParser(scanner);

        TGSong song = parser.getSong();
        TGTrack track = song.getTrack(0);
        TGMeasure measure = track.getMeasure(0);
        List<TGBeat> beats = measure.getBeats();

        Stroke expectedStroke = new Stroke();
        assertEquals(1, beats.size());

        // Beat 0 tick 0 is note 4/2 dur
        TGBeat beat = beats.get(0);
        TGVoice voice = beat.getVoice(0);
        expectedStroke.addNote(1, 3);
        assertVoiceHasNotes(voice, 2, false, expectedStroke);
    }
}

