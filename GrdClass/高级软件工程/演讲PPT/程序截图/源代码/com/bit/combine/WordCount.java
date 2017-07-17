package com.bit.combine;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.ibm.icu.impl.TextTrieMap;

public class WordCount {

	public static class TokenizerMapper

	extends Mapper<Object, Text, Text, Text> {

		private final static IntWritable one = new IntWritable(1);

		private Text word = new Text();
		private Text word1 = new Text();

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");
			if (!tokens[0].equals("") && !tokens[1].equals("")) {
				context.write(new Text(tokens[0]), new Text(tokens[1]));
			}

			// for (int i = 0; i < tokens.length; i++) {
			// if (!tokens[i].equals("1") && !tokens[i].equals("0")) {
			// word.set(tokens[i]);
			// if ((i + 3) < tokens.length) {
			// if (tokens[i + 1].equals("1")) {
			// word1.set("male");
			// } else if (tokens[i + 2].equals("1")
			// && (i + 2) < tokens.length) {
			// word1.set("female");
			// } else if (tokens[i + 3].equals("1")) {
			// word1.set(" ");
			// }
			// context.write(word1, one);
			// }
			// }
			// }

		}
	}

	public static class IntSumReducer

	extends Reducer<Text, Text, Text, Text> {

		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<Text> values, Context context)

		throws IOException, InterruptedException {

			int sum = 0;
			Text text = new Text();
			for (Text val : values) {
				text.set(val);

			}
			context.write(key, text);

		}
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();

		Job job = new Job(conf, "word count");

		job.setJarByClass(WordCount.class);

		job.setMapperClass(TokenizerMapper.class);

		job.setCombinerClass(IntSumReducer.class);

		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(Text.class);

		// FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileInputFormat.addInputPath(job, new Path("logfile/egofeat1/"));

		FileOutputFormat.setOutputPath(job, new Path(
				"hdfs://master:9000/gplus/output/position1/"));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}