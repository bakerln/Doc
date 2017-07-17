import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.hadoop.conf.Configuration;
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

public class Circle {
	private static final String HDFS = "hdfs://master:9000";
	static Map<String, String> mapA = new HashMap<String, String>();

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

			if (flag.equals("circle")) {

				String[] tokens = values.toString().split("\t");

				String row = tokens[0];
				for (int i = 1; i < tokens.length; i++) {
					context.write(new Text(row), new Text("BA " + tokens[i]));
				}

			} else if (flag.equals("position")) {
				String[] tokens = values.toString().split("\t");
				if (tokens.length == 2) {
					Text k = new Text(tokens[0]);
					Text v = new Text("AB " + tokens[1]);
					context.write(k, v);
				}
			}
		}
	}

	public static class PageRankReducer extends Reducer<Text, Text, Text, Text> {
		int sum = 0;

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			for (Text line : values) {
				String vals = line.toString();
				if (vals.startsWith("AB")) {
					System.out.println(vals.toString());
					String[] tokenA = vals.split(" ");
					if (tokenA.length == 2) {
						
						context.write(new Text(tokenA[1]), key);
					}

					// context.write(key, new Text(tokenA[1]));
				} else if (vals.startsWith("BA")) {

					String[] tokenB = vals.split(" ");
					mapA.put(tokenB[1], key.toString());
					//context.write(key, new Text(tokenB[1]));
				} else {

					// context.write(key, new Text("123"));
					Iterator<String> iterA = mapA.keySet().iterator();
					while (iterA.hasNext()) {
						String idx = iterA.next();
						//System.out.println("----------" + line.toString());
						if (line.toString().equals(idx)) {
							context.write(new Text(mapA.get(idx)),key);
						}

					}
				}
			}

		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {

		JobConf conf = HdfsDAO.config();
		String localFile = "logfile/circle/100129275726588145876.circles";

		String inputPath1 = HDFS + "/gplus/output/position/";
		String inputPath2 = HDFS + "/gplus/input/circle/";

		String outputPath = HDFS + "/gplus/output/circle/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		hdfs.rmr(outputPath);
		// hdfs.rmr(inputPath1);
		hdfs.rmr(inputPath2);
		// hdfs.mkdirs(inputPath1);
		hdfs.mkdirs(inputPath2);
		hdfs.copyFile(localFile, inputPath2);
		// hdfs.CopyDirectory(localFile1, inputPath1);
		// hdfs.CopyDirectory(localFile2, inputPath2);
		hdfs.ls(inputPath1);
		hdfs.ls(inputPath2);

		Job job = new Job(conf);
		job.setJarByClass(Circle.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(PageRankMapper.class);
		job.setCombinerClass(PageRankReducer.class);
		job.setReducerClass(PageRankReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.setInputPaths(job, new Path(inputPath1), new Path(
				inputPath2));

		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.waitForCompletion(true);

	}
}
