import java.io.*;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PageRankEC {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: PageRankEC <input path> <output path> iterations");
            System.exit(-1);
        }

        int iters = Integer.parseInt(args[2]);

        for (int i = 0; i < iters; i++) {
            Job job = new Job();
            job.setJarByClass(PageRankEC.class);
            job.setJobName("PageRank: iter " + (i + 1));
            job.setNumReduceTasks(1);

            FileInputFormat.addInputPath(job, new Path(i == 0 ? args[0] : args[1] + i + "/part-r-00000"));
            FileOutputFormat.setOutputPath(job, new Path(i == (iters - 1) ? args[1] : args[1] + (i + 1)));

            job.setMapperClass(PageRankMapper.class);
            job.setReducerClass(PageRankReducer.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            job.waitForCompletion(true);
        }
    }
}
