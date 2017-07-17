package com.bit.combine;
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

/**
 * 将ego中上过大学的的ID整合到一个文件中
 * step 2
 * @author hadoop
 *
 */
public class FeatCombine {

	private static final String HDFS = "hdfs://master:9000";
	public static class TokenizerMapper

	extends Mapper<Object, Text, Text, Text> {

		private Text word = new Text();

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				if (!tokens[i].equals("1") && !tokens[i].equals("0")) {
					word.set(tokens[i]);
					String str = new String();
					for (int j = i + 1; j < tokens.length; j++) {

						if (!tokens[j].equals("1") && !tokens[j].equals("0")) {
							break;
						}
						str +=" "+tokens[j];
					}
					System.out.println(str);
					context.write(word, new Text(str.toString()));
				}
			}
		
		}
	}

	public static class IntSumReducer
	extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values,
				Context context)

		throws IOException, InterruptedException {

			String str = new String();

			for (Text val : values) {

				str = val.toString();

			}


			context.write(key, new Text(str));
		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = HdfsDAO.config();
		
		String inputPath = HDFS + "/gplus/input/position/ego_university/";
		String outputPath = HDFS + "/gplus/output/combine/";
		
		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		hdfs.rmr(outputPath);
		

		Job job = new Job(conf, "Feat Combine");

		job.setJarByClass(FeatCombine.class);

		job.setMapperClass(TokenizerMapper.class);

		job.setCombinerClass(IntSumReducer.class);

		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(Text.class);

		// FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileInputFormat.addInputPath(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.waitForCompletion(true);

	}

}