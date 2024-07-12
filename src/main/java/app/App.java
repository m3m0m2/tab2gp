package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;   // To read text files

public class App
{
    public static void main( String[] args )
    {
        if (args.length != 2)
        {
            System.out.println("Usage: tab2gp <input.txt> <output.gp>");
            return;
        }

        try
        {
            String inFileName = args[0];
            File inFile = new File(inFileName);
            Scanner scanner = new Scanner(inFile);

            TxtParser parser = new TxtParser(scanner);
            scanner.close();

            parser.saveAsGP(args[1]);
        } catch (FileNotFoundException e)
        {
            System.out.println("Exception error:");
            e.printStackTrace();
        }
    }
}
