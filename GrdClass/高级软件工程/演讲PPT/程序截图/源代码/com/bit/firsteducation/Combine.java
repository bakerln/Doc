package com.bit.firsteducation;

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

public class Combine {
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
			if (flag.equals("first_sex")) {
				Text k = new Text(tokens[0]);
				Text v = new Text("A " + tokens[1]);
				context.write(k, v);

			} else if (flag.equals("education_order")) {
				Text k = new Text(tokens[0]);
				Text v = new Text("B " + tokens[1]);
				context.write(k, v);
				//System.out.println(k + v.toString());
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
					mapB.put(key.toString(), tokenB[1]);

				} else {
					Text male = new Text("0");
					Text education = new Text("0");
					Iterator<String> iterB = mapB.keySet().iterator();
					while (iterB.hasNext()) {
						String idx = iterB.next();
						if (key.toString().equals(idx)) {
							System.out.println(idx);
							if (line.toString().equals("男")) {
								male.set("0");
								education.set("1");
							}
							if (line.toString().equals("女")) {
								male.set("1");
								education.set("1");
							}

						} 

					}
					context.write(male, education);

				}
			}

		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {

		JobConf conf = HdfsDAO.config();
		String localFile1 = HDFS + "/gplus/output/education/2/first_sex/";
		String localFile2 = HDFS + "/gplus/output/education/1/education_order/";

		String outputPath = HDFS + "/gplus/output/education/3/combine/";

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
		job.setJarByClass(Combine.class);

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
