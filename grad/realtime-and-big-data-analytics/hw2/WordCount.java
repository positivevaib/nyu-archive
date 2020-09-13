import java.io.*;
import java.util.*;
import java.util.regex.*;

public class WordCount {
    public static void main(String[] args) throws Exception {
        // Read input and output paths
        if (args.length != 2) {
            System.err.println("Usage: JavaWordCount <input path> <output path>");
            System.exit(-1);
        }

        // Read input file and store all lines in an ArrayList
        ArrayList<String> inputData = new ArrayList<>();

        File inputFile = new File(args[0]);
        try {
            Scanner in = new Scanner(inputFile);
        
            while (in.hasNextLine())
                inputData.add(in.nextLine());
        
            in.close();
        }
        catch(Exception e) {
            System.out.println("Input file not found.");
            System.exit(-1);
        }

        // Create new output file
        File outputFile = new File(args[1]);
        if (!outputFile.createNewFile()) {
            System.out.println("Output file already exists.");
            System.exit(-1);
        }

        // Instantiate FileWriter object
        FileWriter out = new FileWriter(args[1]);

        // Search terms
        String[] searchTerms = {"hackathon", "Dec", "Chicago", "Java"};

        // Word count search terms
        for (String term: searchTerms) {
            Pattern pattern = Pattern.compile("\\b" + term + "\\b", Pattern.CASE_INSENSITIVE); 

            int freq = 0;
            for (int i = 0; i < inputData.size(); i++) {
                Matcher matcher = pattern.matcher(inputData.get(i));
                if (matcher.find())
                    freq++;
            }

            out.write(term + " " + freq + "\n");
        }

        out.close();
    }
}
