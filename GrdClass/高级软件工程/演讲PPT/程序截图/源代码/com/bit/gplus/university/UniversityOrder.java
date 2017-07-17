package com.bit.gplus.university;

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

public class UniversityOrder {

	private static final String HDFS = "hdfs://master:9000";

	public static class TokenizerMapper

	extends Mapper<Object, Text, IntWritable, Text> {

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");
			if (tokens.length == 2) {
				
					context.write(new IntWritable(Integer.valueOf(tokens[1])), new Text(tokens[0]));
				
			}

		}
	}

	public static class IntSumReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
		private static Text text = new Text();

		public void reduce(IntWritable key, Iterable<Text> values, Context context)

		throws IOException, InterruptedException {

			for (Text val : values) {
				context.write(key, val);
			}

			
		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = HdfsDAO.config();

		String inputLoca = "logfile/featnames/";

		String inputPath = HDFS + "/gplus/output/university/";
		String outputPath = HDFS + "/gplus/output/university1/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);


		hdfs.rmr(outputPath);
		Job job = new Job(conf, "university count");

		job.setJarByClass(UniversityOrder.class);

		job.setMapperClass(TokenizerMapper.class);

		job.setCombinerClass(IntSumReducer.class);

		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(IntWritable.class);

		job.setOutputValueClass(Text.class);

		// FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileInputFormat.addInputPath(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}