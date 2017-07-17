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

public class CircleOrder {
	private static final String HDFS = "hdfs://master:9000";

	public static class TokenizerMapper

	extends Mapper<Object, Text, Text, Text> {

		private final static IntWritable one = new IntWritable(1);

		private Text word = new Text();
		private Text word1 = new Text();

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");

			context.write(new Text(tokens[1]), new Text(tokens[0]));
		}
	}

	public static class IntSumReducer

	extends Reducer<Text, Text, Text, Text> {

		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<Text> values, Context context)

		throws IOException, InterruptedException {

			int sum = 0;

			for (Text val : values) {

				context.write(key, val);

			}

		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = HdfsDAO.config();
		String inputPath = HDFS + "/gplus/output/circleCount/";
		String outputPath = HDFS + "/gplus/output/circleOrder/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		// hdfs.rmr(inputPath);
		// hdfs.mkdirs(inputPath);
		hdfs.rmr(outputPath);
		hdfs.ls(inputPath);
		Job job = new Job(conf, "circle count");

		job.setJarByClass(CircleOrder.class);

		job.setMapperClass(TokenizerMapper.class);

		job.setCombinerClass(IntSumReducer.class);

		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(Text.class);

		// FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileInputFormat.addInputPath(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}