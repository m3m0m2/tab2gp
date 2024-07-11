package app;

import java.util.Map;
import java.util.TreeMap;

public class Stroke
{
    private Map<Integer, Integer> notes;

    public Stroke()
    {
        notes = new TreeMap<Integer, Integer>();
    }

    public void addNote(int string, int fret)
    {
        notes.put(string, fret);
    }

    public Map<Integer, Integer> getNotes()
    {
        return notes;
    }
}
