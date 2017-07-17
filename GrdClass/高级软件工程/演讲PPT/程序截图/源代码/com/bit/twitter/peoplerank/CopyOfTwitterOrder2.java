package com.bit.twitter.peoplerank;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.bit.hdfs.HdfsDAO;

/**
 * step 1
 * 
 * @author hadoop
 *
 */
public class CopyOfTwitterOrder2 {
	private static final String HDFS = "hdfs://master:9000";
	static Map<String, String> mapB = new HashMap<String, String>();
	

	public static class PageRankMapper extends
			Mapper<LongWritable, Text, Text, Text> {
		private String flag;// tmp1 or result

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			FileSplit split = (FileSplit) context.getInputSplit();
			flag = split.getPath().getParent().getName();// 判断读的数据集
		}

		@Override
		public void map(LongWritable key, Text values, Context context)
				throws IOException, InterruptedException {
			// System.out.println(flag);
			String[] tokens = values.toString().split("\t");
			
			context.write(new Text(tokens[1]),
					new Text(tokens[0]));
		}
	}

	public static class PageRankReducer extends
			Reducer<Text, Text, Text, Text> {
		 int sum = 0;
	

		@Override
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			sum++;
			for (Text line : values) {
				context.write(key, line);
				
			}
			
		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {

		JobConf conf = HdfsDAO.config();

		String inputPath = HDFS + "/twitter/output/twitter_combined6/";
		String outputPath = HDFS + "/twitter/output/twitter_combined7/";

		String inputLocal = "logfile/gplus_combine/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		hdfs.rmr(outputPath);
		if (!hdfs.exists(inputPath)) {
			hdfs.mkdirs(inputLocal);
			hdfs.CopyDirectory(inputLocal, inputPath);
		}

		Job job = new Job(conf);
		job.setJarByClass(CopyOfTwitterOrder2.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(PageRankMapper.class);
		//job.setCombinerClass(PageRankReducer.class);
		job.setReducerClass(PageRankReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.setInputPaths(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.waitForCompletion(true);

	}
}
