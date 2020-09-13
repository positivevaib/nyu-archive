import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class WordCountMapper extends Mapper<Object, Text, Text, IntWritable> {
    private static final String[] searchTerms = {"hackathon", "Dec", "Chicago", "Java"};

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        
        for (String term: searchTerms) {
            Pattern pattern = Pattern.compile("\\b" + term + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);

            if (matcher.find())
                context.write(new Text(term), new IntWritable(1));
            else
                context.write(new Text(term), new IntWritable(0));
        }
    }
}
