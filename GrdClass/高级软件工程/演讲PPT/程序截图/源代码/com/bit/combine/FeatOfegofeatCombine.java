package com.bit.combine;

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
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;

import com.bit.hdfs.HdfsDAO;

/**
 * 计算上过大学的ego的所有feat中上过大学和没上过大学
 * step 6
 * @author hadoop
 *
 */
public class FeatOfegofeatCombine {
	private static final String HDFS = "hdfs://master:9000";
	static Map<String, String> mapB = new HashMap<String, String>();
	private static boolean flag = true;

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
			if (flag.equals("combine")) {
				
				Text k = new Text(tokens[0]);
				Text v = new Text("A " + tokens[1]);
				context.write(k, v);

			} else if (flag.equals("combine1")) {
			
				Text k = new Text(tokens[0]);
				Text v = new Text("B " );
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
					
					mapB.put(key.toString(), tokenA[1]);
				} else if (vals.startsWith("B")) {
					mapB.put(key.toString(), " ");
					context.write(key, new Text(" "));
				} else {
					//System.out.println("-----------------------------------------------");
					Iterator<String> iterB = mapB.keySet().iterator();
					if (flag) {
						while (iterB.hasNext()) {
							String idx = iterB.next();
							if (mapB.get(idx).equals("university")) {
								context.write(new Text("0"), new Text("1"));
							}else {
								context.write(new Text("0"), new Text("0"));
							}
						
							
						}
						flag = false;
					}
					

				}
			}
			

		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {

		JobConf conf = HdfsDAO.config();
//		String localFile1 = "logfile/feat1/";
//		String localFile2 = "logfile/featnames1/";

		String inputPath1 = HDFS + "/gplus/output/combine/";
		String inputPath2 = HDFS + "/gplus/output/combine1/";

		String outputPath = HDFS + "/gplus/output/combine_university/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		hdfs.rmr(outputPath);
//		 hdfs.rmr(inputPath1);
//		 hdfs.rmr(inputPath2);
//		 hdfs.mkdirs(inputPath1);
//		 hdfs.mkdirs(inputPath2);
		// hdfs.copyFile(localFile1, inputPath1);
		// hdfs.copyFile(localFile2, inputPath2);
		// hdfs.CopyDirectory(localFile1, inputPath1);
		// hdfs.CopyDirectory(localFile2, inputPath2);
		hdfs.ls(inputPath1);
		hdfs.ls(inputPath2);

		Job job = new Job(conf);
		job.setJarByClass(FeatOfegofeatCombine.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(PageRankMapper.class);
		job.setCombinerClass(PageRankReducer.class);
		job.setReducerClass(PageRankReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.setInputPaths(job, new Path(inputPath1), new Path(inputPath2));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.waitForCompletion(true);

	}
}
