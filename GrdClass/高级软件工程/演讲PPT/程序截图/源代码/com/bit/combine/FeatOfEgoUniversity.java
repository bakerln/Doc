package com.bit.combine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.fs.FSDataInputStream;
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
import org.apache.hadoop.util.LineReader;

import com.bit.hdfs.HdfsDAO;

/**
 * 查看上过大学的ego中的feat中上过大学的 
 * step 3
 * 
 * @author hadoop
 *
 */
public class FeatOfEgoUniversity {
	private static final String HDFS = "hdfs://master:9000";

	private static boolean flag1 = true;
	private static boolean flag2 = true;
	private static boolean flag3 = true;

	static Map<String, String> mapB = new HashMap<String, String>();
	private static String name = new String();

	public static class PageRankMapper extends
			Mapper<LongWritable, Text, Text, Text> {
		private String flag;// tmp1 or result

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			FileSplit split = (FileSplit) context.getInputSplit();
			flag = split.getPath().getParent().getName();// 判断读的数据集
			// name = context.getConfiguration().get("name");
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

			} else if (flag.equals("featnames_all")) {
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
					
						if (str[0].equals("university")) {
							mapB.put(key.toString(), str[0]);
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
		String localEgofeat = "logfile/egofeat/";
		String localFeat = "logfile/feat/";
		String localFeatnames = "logfile/featnames/";

		String outputPath = HDFS + "/gplus/output/ego_feat_uni/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);

		hdfs.rmr(outputPath);
	

		FileSystem fs = FileSystem.get(URI.create(HDFS), conf);

		 if (!hdfs.exists(HDFS + "/gplus/input/position/feat_all/")) {
		 hdfs.mkdirs(HDFS + "/gplus/input/position/feat_all/");
		 flag3 = false;
		 }
		// 获取源文件夹当前下的文件或目录
		File[] egofeat = (new File(localEgofeat)).listFiles();
		File[] feat = (new File(localFeat)).listFiles();
		File[] featnames = (new File(localFeatnames)).listFiles();

		FSDataInputStream os = fs.open(new Path(HDFS
				+ "/gplus/output/combine/part-r-00000"));

		LineReader in = new LineReader(os, conf);
		Text text = new Text();
		while (in.readLine(text) > 0) {
			String[] str1 = text.toString().split("\t");

			for (int i = 0; i < egofeat.length; i++) {
				if ((egofeat[i].getName().substring(0, egofeat[i].getName()
						.length() - 8)).equals(str1[0])) {

					for (int k = 0; k < feat.length; k++) {
						if (str1[0].equals(feat[k].getName().substring(0,
								feat[k].getName().length() - 5))) {

							for (int j = 0; j < featnames.length; j++) {
								if (str1[0]
										.equals(featnames[j].getName()
												.substring(
														0,
														featnames[j].getName()
																.length() - 10))) {
									System.out.println(str1[0]);
									// this real main function

									String input1 = "logfile/feat/"
											+ feat[k].getName();
									String input2 = "logfile/featnames/"
											+ featnames[j].getName();
									String input3 = "logfile/egofeat/"
											+ egofeat[i].getName();

									String inputFeat = HDFS
											+ "/gplus/input/position/feat_all/"
											+ feat[k].getName();
									String inputFeatnames = HDFS
											+ "/gplus/input/position/featnames_all/"
											+ featnames[j].getName();
								
//									 if (flag3 == false) {
//									 hdfs.copyFile(input1, inputFeat);
//									 }
									
									
									Job job = new Job(conf);
									job.setJarByClass(FeatOfEgoUniversity.class);
									name = feat[k].getName().substring(0,
											feat[k].getName().length() - 6);

									job.setOutputKeyClass(Text.class);
									job.setOutputValueClass(Text.class);

									job.setMapperClass(PageRankMapper.class);
									job.setCombinerClass(PageRankReducer.class);
									job.setReducerClass(PageRankReducer.class);

									job.setInputFormatClass(TextInputFormat.class);
									job.setOutputFormatClass(TextOutputFormat.class);
									FileInputFormat.setInputPaths(job,
											new Path(input1), new Path(
													inputFeatnames));

									FileOutputFormat.setOutputPath(job,
											new Path(outputPath + name));

									job.waitForCompletion(true);
									hdfs.rename(outputPath + name
											+ "/part-r-00000", outputPath
											+ name + "/" + name);
									hdfs.download(outputPath + name + "/"
											+ name, "logfile/feat1/");

									hdfs.mkdirs(HDFS
											+ "/gplus/output/ego_feat_univ/");
									hdfs.upload("logfile/feat1/" + name, HDFS
											+ "/gplus/output/ego_feat_univ/");

								}
							}
						}

					}

				}
			}

			// fs.copyFromLocalFile(new Path(local + file[i].getName()), new
			// Path(
			// remote + file[i].getName()));
		}
		os.close();

	}
}
