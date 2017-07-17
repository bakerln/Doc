package com.bit.combine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import com.sun.xml.bind.v2.runtime.Name;

/**
 * 计算ego中上过大学的人
 * step 1
 * @author hadoop
 *
 */
public class EgoUniversity {
	private static final String HDFS = "hdfs://master:9000";
	static Map<String, String> mapB = new HashMap<String, String>();
	private static String name = new String();
	private static boolean flag1 = true;
	private static boolean flag2 = true;

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
			//System.out.println(flag);
			// System.out.println(name + "------------");
			String[] tokens = values.toString().split(" ");
			if (flag.equals("egofeat")) {
				for (int i = 0; i < tokens.length; i++) {

					if (tokens[i].equals("1")) {
						Text k = new Text();
						k.set(name);
						Text v = new Text("A " + String.valueOf(i));

						context.write(k,v); 
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
						// mapB.put(key.toString(), tokenB[1]);

						// if (str[1].equals("1")) {
						// mapB.put(key.toString(), "男");
						// } else if (str[1].equals("2")) {
						// mapB.put(key.toString(), "女");
						// } else if(str[1].equals("3")){
						// mapB.put(key.toString(), "人妖");
						// }
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
		String localegoFeat = "logfile/egofeat/";
		String localfeatnames = "logfile/featnames/";

		String outputPath = HDFS + "/gplus/output/position/";

		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		if (!hdfs.exists(HDFS + "/gplus/input/position/ego_university/")) {
			hdfs.mkdirs(HDFS + "/gplus/input/position/ego_university/");
			hdfs.rmr(outputPath);
			if (!hdfs.exists(HDFS + "/gplus/input/position/egofeat/")) {
				hdfs.mkdirs(HDFS + "/gplus/input/position/egofeat/");

				flag1 = false;
			}
			if (!hdfs.exists(HDFS + "/gplus/input/position/featnames_all/")) {
				hdfs.mkdirs(HDFS + "/gplus/input/position/featnames_all/");
				
				flag2 = false;
			}

			// 获取源文件夹当前下的文件或目录
			File[] file1 = (new File(localegoFeat)).listFiles();
			File[] file2 = (new File(localfeatnames)).listFiles();
			for (int i = 0; i < file1.length; i++) {
				for (int j = 0; j < file2.length; j++) {
					if ((file1[i].getName().substring(0, file1[i].getName()
							.length() - 9)).equals(file2[j].getName()
							.substring(0, file2[j].getName().length() - 11))) {

						String input1 = "logfile/egofeat/" + file1[i].getName();
						String input2 = "logfile/featnames/"
								+ file2[j].getName();
//						BufferedReader bReader = new BufferedReader(new FileReader(input1));
//						System.out.println(file1[i].getName());
						
						String inputEgofeat = HDFS
								+ "/gplus/input/position/egofeat/"
								+ file1[i].getName();
						String inputFeatnames = HDFS
								+ "/gplus/input/position/featnames_all/"
								+ file2[j].getName();
						if (flag1 == false) {
							hdfs.copyFile(input1, inputEgofeat);
						}
						if (flag2 == false) {
							hdfs.copyFile(input2, inputFeatnames);
						}

						Job job = new Job(conf);
						job.setJarByClass(EgoUniversity.class);
						name = file1[i].getName().substring(0,
								file1[i].getName().length() - 8);

						job.setOutputKeyClass(Text.class);
						job.setOutputValueClass(Text.class);

						job.setMapperClass(PageRankMapper.class);
						job.setCombinerClass(PageRankReducer.class);
						job.setReducerClass(PageRankReducer.class);

						job.setInputFormatClass(TextInputFormat.class);
						job.setOutputFormatClass(TextOutputFormat.class);
						FileInputFormat.setInputPaths(job, new Path(
								inputEgofeat), new Path(inputFeatnames));

						FileOutputFormat.setOutputPath(job, new Path(outputPath
								+ file1[i].getName()));

						job.waitForCompletion(true);

						hdfs.rename(outputPath + file1[i].getName()
								+ "/part-r-00000",
								outputPath + file1[i].getName() + "/"
										+ file1[i].getName());
						hdfs.download(outputPath + file1[i].getName() + "/"
								+ file1[i].getName(), "logfile/egofeat2/");
						hdfs.upload("logfile/egofeat2/" + file1[i].getName(),
								HDFS + "/gplus/input/position/ego_university/"
										+ file1[i].getName());
					}

				}

			}
		}

	}
}
