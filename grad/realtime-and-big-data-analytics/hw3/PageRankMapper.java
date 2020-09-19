import java.io.*;
import java.util.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class PageRankMapper extends Mapper<Object, Text, Text, Text> {
    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String[] line = value.toString().split("\\s+");
        int numLinks = line.length - 2;

        String links = "";
        for (int i = 1; i < (line.length - 1); i++) {
            links += line[i] + " ";
            context.write(new Text(line[i]), new Text(line[0] + ", " + (Double.parseDouble(line[line.length - 1])/numLinks))); 
        }

        context.write(new Text(line[0]), new Text(links.trim()));
    }
}

