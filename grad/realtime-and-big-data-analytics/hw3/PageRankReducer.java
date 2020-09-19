import java.io.*;
import java.util.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PageRankReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String links = "";
        double pr = 0;

        for (Text value: values) {
            if (value.toString().length() > 1 && value.toString().charAt(1) == ',')
                pr += Double.parseDouble(value.toString().split(" ")[1]);
            else
                links += value.toString();
        }

        context.write(key, new Text(links + " " + pr));
    }
}
