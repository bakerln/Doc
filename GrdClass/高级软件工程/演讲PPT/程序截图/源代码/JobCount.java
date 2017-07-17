import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.jobconf_005fhistory_jsp;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.bit.hdfs.HdfsDAO;

public class JobCount {
	private static final String HDFS = "hdfs://master:9000";

	public static class TokenizerMapper

	extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);

		private Text word = new Text();
		private Text word1 = new Text();

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				if (!tokens[i].equals("1") && !tokens[i].equals("0")) {
					word.set(tokens[i]);
					if ((i + 610) < tokens.length) {
						if (tokens[i + 139].equals("1")
								|| tokens[i + 140].equals("1")
								|| tokens[i + 141].equals("1")) {
							word1.set("系统管理员");
						} else if (tokens[i + 150].equals("1")
								|| tokens[i + 151].equals("1")
								|| tokens[i + 152].equals("1")) {
							word1.set("分析人员");
						} else if (tokens[i + 232].equals("1")
								|| tokens[i + 233].equals("1")
								|| tokens[i + 234].equals("1")
								|| tokens[i + 235].equals("1")
								|| tokens[i + 236].equals("1")) {
							word1.set("设计人员");
						} else if (tokens[i + 237].equals("1")
								|| tokens[i + 238].equals("1")
								|| tokens[i + 239].equals("1")
								|| tokens[i + 240].equals("1")) {
							word1.set("开发人员");
						} else if (tokens[i + 264].equals("1")
								|| tokens[i + 265].equals("1")
								|| tokens[i + 266].equals("1")
								|| tokens[i + 267].equals("1")
								|| tokens[i + 268].equals("1")
								|| tokens[i + 269].equals("1")
								|| tokens[i + 336].equals("1")) {
							word1.set("工程师");
						} else if (tokens[i + 198].equals("1")
								|| tokens[i + 199].equals("1")
								|| tokens[i + 200].equals("1")
								|| tokens[i + 201].equals("1")) {
							word1.set("代码");
						} else if (tokens[i + 315].equals("1")) {
							word1.set("黑客");
						} else if (tokens[i + 343].equals("1")) {
							word1.set("IOS");
						} else if (tokens[i + 198].equals("154")) {
							word1.set("Android");
						} else if (tokens[i + 364].equals("1")) {
							word1.set("Linux");
						} else if (tokens[i + 406].equals("1")
								|| tokens[i + 407].equals("1")
								|| tokens[i + 408].equals("1")) {
							word1.set("网络");
						} else if (tokens[i + 465].equals("1")
								|| tokens[i + 466].equals("1")
								|| tokens[i + 467].equals("1")
								|| tokens[i + 468].equals("1")
								|| tokens[i + 469].equals("1")
								|| tokens[i + 470].equals("1")
								|| tokens[i + 471].equals("1")
								|| tokens[i + 472].equals("1")
								|| tokens[i + 473].equals("1")
								|| tokens[i + 474].equals("1")
								|| tokens[i + 475].equals("1")) {
							word1.set("编码");
						} else if (tokens[i + 475].equals("1")) {
							word1.set("python");
						} else if (tokens[i + 505].equals("1")
								|| tokens[i + 506].equals("1")
								|| tokens[i + 507].equals("1")
								|| tokens[i + 508].equals("1")) {
							word1.set("软件");
						} else if (tokens[i + 545].equals("1")
								|| tokens[i + 546].equals("1")
								|| tokens[i + 547].equals("1")
								|| tokens[i + 548].equals("1")
								|| tokens[i + 549].equals("1")
								|| tokens[i + 550].equals("1")
								|| tokens[i + 551].equals("1")) {
							word1.set("技术人员");
						} else if (tokens[i + 581].equals("1")
								|| tokens[i + 582].equals("1")) {
							word1.set("网管");
						} else if (tokens[i + 602].equals("1")

						) {
							word1.set("youtube");
						}
						context.write(word1, one);
					}
				}
			}
			// while (itr.hasMoreTokens()) {
			//
			// word.set(itr.nextToken());
			// }
		}
	}

	public static class IntSumReducer

	extends Reducer<Text, IntWritable, Text, IntWritable> {

		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context)

		throws IOException, InterruptedException {

			int sum = 0;

			for (IntWritable val : values) {

				sum += val.get();

			}

			result.set(sum);

			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = HdfsDAO.config();
		String localFile = "logfile/test1/100129275726588145876.feat";
		String inputPath = HDFS +"/gplus/jobInput/";
		String outputPath = HDFS+"/gplus/jobOutput";
		
		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		hdfs.rmr(inputPath);
		hdfs.mkdirs(inputPath);
		hdfs.copyFile(localFile, inputPath);
		hdfs.ls(inputPath);
		Job job = new Job(conf, "word count");

		job.setJarByClass(JobCount.class);

		job.setMapperClass(TokenizerMapper.class);

		job.setCombinerClass(IntSumReducer.class);

		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(IntWritable.class);

		// FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileInputFormat.addInputPath(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}