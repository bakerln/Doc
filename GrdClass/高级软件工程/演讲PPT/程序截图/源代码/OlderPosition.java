import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.bit.hdfs.HdfsDAO;

public class OlderPosition {
	private static final String HDFS = "hdfs://master:9000";

	public static class TokenizerMapper

	extends Mapper<Object, Text, IntWritable, Text> {

		private final static IntWritable one = new IntWritable(1);

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");
			if (tokens.length == 2) {
				context.write(new IntWritable(Integer.valueOf(tokens[1])),
						new Text(tokens[0]));
			}

		}
	}

	public static class IntSumReducer

	extends Reducer<IntWritable, Text, IntWritable, Text> {

		public void reduce(IntWritable key, Iterable<Text> values,
				Context context)

		throws IOException, InterruptedException {


			for (Text val : values) {

				context.write(key, val);

			}


			
		}
	}

	public static void run() throws Exception {
		TypeOfPosition.run();
		JobConf conf = HdfsDAO.config();

		String inputPath = HDFS + "/gplus/output/typeOfPosition/";
		String outputPath = HDFS + "/gplus/output/OlderPosition/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		hdfs.rmr(outputPath);
		hdfs.ls(inputPath);

		Job job = new Job(conf);

		job.setJarByClass(OlderPosition.class);

		job.setMapperClass(TokenizerMapper.class);

		job.setCombinerClass(IntSumReducer.class);

		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(IntWritable.class);

		job.setOutputValueClass(Text.class);

		// FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileInputFormat.addInputPath(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.waitForCompletion(true);

	}

}