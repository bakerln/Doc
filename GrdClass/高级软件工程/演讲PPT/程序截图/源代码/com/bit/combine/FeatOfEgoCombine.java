package com.bit.combine;

import java.io.IOException;




import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.bit.hdfs.HdfsDAO;

/**
 * 合并ego中上过大学中相对应feat已经上过大学的人
 * step 5
 * @author hadoop
 *
 */
public class FeatOfEgoCombine {
	private static final String HDFS = "hdfs://master:9000";
	public static class TokenizerMapper

	extends Mapper<Object, Text, Text, Text> {

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");

			context.write(new Text(tokens[0]), new Text(tokens[1]));
		

		}
	}

	public static class IntSumReducer extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context)

		throws IOException, InterruptedException {

			String str = new String();

			for (Text val : values) {

				str = val.toString();
				//System.out.println(str);
			}

			context.write(key, new Text(str));
		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = HdfsDAO.config();

		String inputPath = HDFS + "/gplus/output/ego_feat_univ/";
		String outputPath = HDFS + "/gplus/output/combine/";
		
		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		hdfs.rmr(outputPath);
		Job job = new Job(conf, "word count");

		job.setJarByClass(FeatOfEgoCombine.class);

		job.setMapperClass(TokenizerMapper.class);

		job.setCombinerClass(IntSumReducer.class);

		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(Text.class);

		// FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileInputFormat.addInputPath(job, new Path(
				inputPath));

		FileOutputFormat.setOutputPath(job, new Path(
				outputPath));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}