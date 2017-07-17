package com.bit.twitter.peoplerank;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
public class TwitterTransf {
	private static final String HDFS = "hdfs://master:9000";
	static Map<String, String> mapB = new HashMap<String, String>();
	// static Map<String, String> mapA = new HashMap<String, String>();
	static int index = 0;
	static boolean f = true;

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

			if (flag.equals("twitter_combined")) {

				String[] tokens = values.toString().split(" ");
				context.write(new Text(tokens[0]), new Text("A " + tokens[1]));

			} else if (flag.equals("twitter_combined4")) {
				String[] tokens = values.toString().split("\t");
				Text k = new Text(tokens[0]);
				Text v = new Text("B " + tokens[1]);

				mapB.put(tokens[0], tokens[1]);
				// mapA.put(tokens[0], tokens[1]);

				context.write(k, v);

			}
		}
	}

	public static class PageRankReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			for (Text line : values) {

				String vals = line.toString();
				if (vals.startsWith("A")) {
					String str[] = vals.split(" ");
					context.write(key, new Text(str[1]));
					Iterator<String> iterB = mapB.keySet().iterator();
					while (!iterB.hasNext()) {
						String idx = iterB.next();
						if (key.toString().equals(idx)) {
							
							Iterator<String> iterA = mapB.keySet().iterator();
							while (!iterA.hasNext()) {
								String idx1 = iterA.next();
								if (vals.equals(idx1)) {
									context.write(new Text(mapB.get(idx)), new Text(mapB.get(idx1)));
								}
							}
						}
					}
				} else  {
					context.write(key, line);
				} 
			}

		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {

		JobConf conf = HdfsDAO.config();
		String localFile1 = HDFS + "/twitter/output/twitter_combined4/";
		String localFile2 = HDFS + "/twitter/input/twitter_combined/";

		String outputPath = HDFS + "/twitter/output/twitter_combined5/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		while (index < 1) {

			hdfs.rmr(outputPath);
			// hdfs.rmr(inputPath1);
			// hdfs.rmr(inputPath2);
			// hdfs.mkdirs(inputPath1);
			// hdfs.mkdirs(inputPath2);
			// hdfs.copyFile(localFile1, inputPath1);
			// hdfs.copyFile(localFile2, inputPath2);
			// hdfs.CopyDirectory(localFile1, inputPath1);
			// hdfs.CopyDirectory(localFile2, inputPath2);

			Job job = new Job(conf);
			job.setJarByClass(TwitterTransf.class);

			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);

			job.setMapperClass(PageRankMapper.class);
		//	job.setCombinerClass(PageRankReducer.class);
			job.setReducerClass(PageRankReducer.class);

			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			FileInputFormat.setInputPaths(job, new Path(localFile1), new Path(
					localFile2));

			FileOutputFormat.setOutputPath(job, new Path(outputPath));

			job.waitForCompletion(true);
			f = false;
			++index;
			// fs.copyFromLocalFile(new Path(local + file[i].getName()), new
			// Path(
			// remote + file[i].getName()));
		}
	}
}
