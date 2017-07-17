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

public class University {

private static final String HDFS="hdfs://master:9000";
	public static class PageRankMapper extends
			Mapper<Object, Text, IntWritable, IntWritable> {


		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {
			IntWritable X = new IntWritable();
			IntWritable Y = new IntWritable();
			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");
				String[] str1 = tokens[1].split(":");
				if (str1[0].equals("university")) {
					Y.set(1);
				}else{
					Y.set(0);
				}
				if(str1[0].equals("gender")) {
					if(str1[1].equals("1")){
						X.set(1);
					}else{
						X.set(0);
					}
					
				}
				context.write(X, Y);
			

		}
	}

	public static class PageRankReducer extends
			Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {

		public void reduce(IntWritable key, Iterable<IntWritable> values,
				Context context)

		throws IOException, InterruptedException {

			for (IntWritable val : values) {
				context.write(key, val);
			}

			
		}

	}

	public static void main(String[] args) throws Exception {
		//Position.run();
		OlderPosition.run();
		JobConf conf = HdfsDAO.config();
		
		String inputPath = HDFS+"/gplus/output/position/";
		String outputPath = HDFS+"/gplus/output/university/";
		
		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		
		hdfs.rmr(outputPath);
		hdfs.ls(inputPath);

		Job job = new Job(conf);
		job.setJarByClass(University.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(PageRankMapper.class);
		job.setCombinerClass(PageRankReducer.class);
		job.setReducerClass(PageRankReducer.class);

		
		FileInputFormat.setInputPaths(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		job.waitForCompletion(true);

	}

}
