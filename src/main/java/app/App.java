package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;           // To read text files
import org.apache.commons.cli.*;    // Cli parser

public class App
{
    public static Options getCliOptions()
    {
        Options options = new Options();

        Option optInput = new Option("i", "input", true, "Input File");
        optInput.setRequired(true);
        options.addOption(optInput);

        Option optName = new Option("n", "name", true, "Name");
        optName.setRequired(false);
        options.addOption(optName);

        Option optArtist = new Option("a", "artist", true, "Artist");
        optArtist.setRequired(false);
        options.addOption(optArtist);

        Option optAuthor = new Option("A", "author", true, "Author");
        optAuthor.setRequired(false);
        options.addOption(optAuthor);

        Option optWriter = new Option("w", "writer", true, "Writer");
        optWriter.setRequired(false);
        options.addOption(optWriter);

        Option optCopyright = new Option("c", "copyright", true, "Copyright");
        optCopyright.setRequired(false);
        options.addOption(optCopyright);

        Option optDate = new Option("d", "date", true, "Date");
        optDate.setRequired(false);
        options.addOption(optDate);

        return options;
    }

    public static void main(String[] args)
    {
        Options options = getCliOptions();
        CommandLine cmd = null;

        try {
            CommandLineParser cliParser = new DefaultParser();
            cmd = cliParser.parse(options, args);

            if (cmd.getArgList().size() != 1)
                throw new ParseException("Missing output file");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp("tab2gp [opts] -i <input.txt> <output.gp>", options);
            System.exit(1);
        }

        try
        {
            String inFileName = cmd.getOptionValue("input");
            File inFile = new File(inFileName);
            Scanner scanner = new Scanner(inFile);

            TxtParser parser = new TxtParser(scanner);
            scanner.close();

            if (cmd.hasOption("name"))
                parser.getSong().setName(cmd.getOptionValue("name"));

            if (cmd.hasOption("artist"))
                parser.getSong().setArtist(cmd.getOptionValue("artist"));

            if (cmd.hasOption("author"))
                parser.getSong().setAuthor(cmd.getOptionValue("author"));

            if (cmd.hasOption("writer"))
                parser.getSong().setWriter(cmd.getOptionValue("writer"));

            if (cmd.hasOption("copyright"))
                parser.getSong().setCopyright(cmd.getOptionValue("copyright"));

            if (cmd.hasOption("date"))
                parser.getSong().setDate(cmd.getOptionValue("date"));

            String outFileName = cmd.getArgList().get(0);
            parser.saveAsGP(outFileName);
        } catch (FileNotFoundException e)
        {
            System.out.println("Exception error:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
