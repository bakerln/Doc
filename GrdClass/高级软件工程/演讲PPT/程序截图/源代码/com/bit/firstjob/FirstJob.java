package com.bit.firstjob;

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

public class FirstJob {
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
			String[] tokens = values.toString().split(" ");
			if (flag.equals("feat")) {
				String row = tokens[0];
				for (int i = 1; i < tokens.length; i++) {
					// System.out.println(tokens[i]);
					if (tokens[i].equals("1")) {
						Text k = new Text();
						k.set(row);
						Text v = new Text("A " + String.valueOf(i - 1));
						context.write(k, v);
					}
				}

			} else if (flag.equals("featnames")) {
				Text k = new Text(tokens[0]);
				Text v = new Text("B " + tokens[1]);
				context.write(k, v);
				// System.out.println(k + v.toString());
			}
		}
	}

	public static class PageRankReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			for (Text line : values) {
				// System.out.println(key.toString() + "\t" + line);
				String vals = line.toString();
				// context.write(key, new Text(vals));
				if (vals.startsWith("A")) {
					String[] tokenA = vals.split(" ");

					context.write(key, new Text(tokenA[1]));
				} else if (vals.startsWith("B")) {

					String[] tokenB = vals.split(" ");
					String[] str = tokenB[1].split(":");
					if (tokenB.length == 2 && str.length == 2) {
						if (str[0].equals("job_title")) {
							
								mapB.put(key.toString(), str[1]);
							
						}
					}

				} else {
					Iterator<String> iterB = mapB.keySet().iterator();
					while (iterB.hasNext()) {
						String idx = iterB.next();
						if (line.toString().equals(idx)) {
							context.write(key, new Text(mapB.get(idx)));
						}

					}

				}
			}

		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {

		JobConf conf = HdfsDAO.config();
		String localFile1 = "logfile/feat/100129275726588145876.feat";
		String localFile2 = "logfile/featnames/100129275726588145876.featnames";

		String outputPath = HDFS + "/gplus/output/job/job_first/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

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
		job.setJarByClass(FirstJob.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(PageRankMapper.class);
		job.setCombinerClass(PageRankReducer.class);
		job.setReducerClass(PageRankReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.setInputPaths(job, new Path(localFile1), new Path(
				localFile2));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.waitForCompletion(true);

		// fs.copyFromLocalFile(new Path(local + file[i].getName()), new
		// Path(
		// remote + file[i].getName()));

	}
}
