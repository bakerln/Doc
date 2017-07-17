package com.bit.firstjob;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.bit.hdfs.HdfsDAO;


public class JobCount {

	private static final String HDFS = "hdfs://master:9000";

	public static class TokenizerMapper

	extends Mapper<Object, Text, Text, Text> {

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");

			context.write(new Text(tokens[1]), new Text("A "+tokens[0]));

		}
	}

	public static class IntSumReducer extends Reducer<Text, Text, Text, Text> {
		private static Text text = new Text();

		public void reduce(Text key, Iterable<Text> values, Context context)

		throws IOException, InterruptedException {

			int sum = 0;

			for (Text val : values) {
				String str = val.toString();
				if (str.startsWith("A")) {
					String str1[] = str.split(" ");
					++sum;
				}else {
					sum =Integer.valueOf(str);
				}
			}
			text.set(String.valueOf(sum));

			context.write(key, text);
		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = HdfsDAO.config();

		String inputPath = HDFS + "/gplus/output/job/job_order/";
		String outputPath = HDFS + "/gplus/output/job/job_count/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		hdfs.rmr(outputPath);
		Job job = new Job(conf, "word count");

		job.setJarByClass(JobCount.class);

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