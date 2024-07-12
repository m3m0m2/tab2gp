package app;

class FakeTab
{
    private int ticks;
    private int strings;
    private int[][] tab;

    public void setStringsAndTicks(int ticks, int strings)
    {
        this.ticks = ticks;
        this.strings = strings;

        tab = new int[strings][];
        for (int i=0; i<strings; i++)
        {
            tab[i] = new int[ticks];
            for (int j=0; j<ticks; j++)
                tab[i][j] = -1;
        }
    }

    public FakeTab()
    {
        setStringsAndTicks(32, 6);
    }

    public void clear()
    {
        setStringsAndTicks(ticks, strings);
    }

    // Note: string index is 1 based, so between 1-6
    public void setNote(int tick, int string, int fret)
    {
        tab[string - 1][tick] = fret;
    }

    public String toString()
    {
        String txt = "";

        for (int i=0; i<strings; i++)
        {
            txt += "X|";
            for (int j=0; j<ticks; j++)
            {
                if (tab[i][j] < 0)
                    txt += "--";
                else
                {
                    if (tab[i][j] < 10)
                        txt += " ";
                    txt += Integer.toString(tab[i][j] % 100);
                }
            }
            txt += "\n";
        }
        return txt;
    }
}
