package app;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TabTest
{
    private final Tab tab = new Tab();

    private void setUpTab1()
    {
        int time = 3;

        //tab.addNote(time, string, fret);
        tab.addNote(time, 3, 10);
        tab.addNote(time, 4, 11);

        time = 4;
        tab.addNote(time, 4, 12);
    }

    @Test
    public void getStrokeIsNullIfInvalidTime()
    {
        setUpTab1();

        Stroke stroke = tab.getStrokeAt(0);
        assertEquals(null, stroke);
    }

    @Test
    public void getStrokeGivesValidResult()
    {
        setUpTab1();

        Stroke stroke = tab.getStrokeAt(3);
        Map<Integer, Integer> notes = stroke.getNotes();

        Map<Integer, Integer> expectedNotes = Map.of(3, 10, 4, 11);
        assertEquals(expectedNotes, notes);
    }

    @Test
    public void getNextTimeBeforeStart()
    {
        setUpTab1();

        Integer time = tab.getNextTime(0);

        assertTrue(3 == time);
    }

    @Test
    public void getNextTimeAtValidTime()
    {
        setUpTab1();

        Integer time = tab.getNextTime(3);

        assertTrue(4 == time);
    }

    @Test
    public void getNextTimeAtEnd()
    {
        setUpTab1();

        Integer time = tab.getNextTime(4);

        assertTrue(null == time);
    }
}

