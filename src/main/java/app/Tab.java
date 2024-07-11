package app;

import java.util.Map;
import java.util.TreeMap;
import java.util.NavigableMap;

public class Tab
{
    private NavigableMap<Integer, Stroke> strokes;

    public Tab()
    {
        strokes = new TreeMap<Integer, Stroke>();
    }

    public Stroke getStrokeAt(int time)
    {
        return strokes.get(time);
    }

    public Integer getNextTime(int time)
    {
        return strokes.higherKey(time);
    }

    public void addNote(int time, int string, int fret)
    {
        //System.out.printf("addNote: %d %d %d\n", time, string, fret);
        Integer checkKey  = strokes.ceilingKey(time);
        if (checkKey == null || checkKey != time)
            strokes.put(time, new Stroke());
        strokes.get(time).addNote(string, fret);
    }
}
