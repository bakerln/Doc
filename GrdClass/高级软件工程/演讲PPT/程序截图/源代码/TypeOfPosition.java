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

public class TypeOfPosition {
	private static final String HDFS = "hdfs://master:9000";

	public static class PageRankMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);

		private Text word = new Text();
		private Text word1 = new Text();

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			// StringTokenizer itr = new StringTokenizer(value.toString());
			String[] tokens = value.toString().split("\t");
			for (String str : tokens) {
				String[] str1 = str.split(":");
				if (str1[0].equals("job_title")) {
					System.out.println(str1[1]);
					for (int i = 0; i < str1.length; i++) {
						context.write(new Text(str1[1]), one);
					}
				}
			}

		}
	}

	public static class PageRankReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
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

	public static void run() throws Exception {
		//ID_Featnames.run();
		JobConf conf = HdfsDAO.config();

		String inputPath = HDFS + "/gplus/output/position/";
		String outputPath = HDFS + "/gplus/output/typeOfPosition/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		hdfs.rmr(outputPath);
		// hdfs.ls(inputPath);
		Job job = new Job(conf);
		job.setJarByClass(TypeOfPosition.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(PageRankMapper.class);
		job.setCombinerClass(PageRankReducer.class);
		job.setReducerClass(PageRankReducer.class);

		FileInputFormat.setInputPaths(job, new Path(inputPath));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		job.waitForCompletion(true);

	}

}
