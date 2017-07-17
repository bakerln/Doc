package com.bit.kmeans;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters.Counter;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.bit.hdfs.HdfsDAO;

public class KmeansMR {

	private static String FLAG = "KCLUSTER";
	private static final String HDFS="hdfs://master:9000";
		
	public static class TokenizerMapper 
    extends Mapper<Object, Text, Text, Text>{
		
		double[][] centers = new double[Center.k][];
		String[] centerstrArray = null;
		
		@Override
		public void setup(Context context) {
			
			//将放在context中的聚类中心转换为数组的形式，方便使用
			String kmeansS = context.getConfiguration().get(FLAG);
			centerstrArray = kmeansS.split("\t");
			for(int i = 0; i < centerstrArray.length; i++) {
				String[] segs = centerstrArray[i].split(" ");
				centers[i] = new double[segs.length];
				for(int j = 0; j < segs.length; j++) {
					centers[i][j] = Double.parseDouble(segs[j]);
				}
			}
		}
		
		public void map(Object key, Text value, Context context
                 ) throws IOException, InterruptedException {
			
			String line = value.toString();
			String[] segs = line.split(" ");
			double[] sample = new double[segs.length];
			for(int i = 0; i < segs.length; i++) {
				sample[i] = Float.parseFloat(segs[i]);
			}
			//求得距离最近的质心
			double min = Double.MAX_VALUE;
			int index = 0;
			for(int i = 0; i < centers.length; i++) {
				double dis = distance(centers[i], sample);
				if(dis < min) {
					min = dis;
					index = i;
				}
			}
			System.out.println(centerstrArray[index]+"-------"+line);
			context.write(new Text(centerstrArray[index]), new Text(line));
		}
	}

	public static class IntSumReducer 
    extends Reducer<Text,Text,NullWritable,Text> {

		Counter counter = null;
		
		public void reduce(Text key, Iterable<Text> values, 
                    Context context
                    ) throws IOException, InterruptedException {
			
			double[] sum = new double[Center.k];
			int size = 0;
			//计算对应维度上值的加和，存放在sum数组中
			for(Text text : values) {
				String[] segs = text.toString().split(" ");
				for(int i = 0; i < segs.length; i++) {
					sum[i] += Double.parseDouble(segs[i]);
				}
				size ++;
			}
			
			//求sum数组中每个维度的平均值，也就是新的质心
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < sum.length; i++) {
				sum[i] /= size;
				sb.append(sum[i]);
				sb.append(" ");
				//System.out.println(sb.toString());
			}
			
			/**判断新的质心跟老的质心是否是一样的*/
			boolean flag = true;
			String[] centerStrArray = key.toString().split(" ");
			for(int i = 0; i < centerStrArray.length; i++) {
				if(Math.abs(Double.parseDouble(centerStrArray[i]) - sum[i]) > 0.00000000001) {
					flag = false;
					break;
				}
			}
			//如果新的质心跟老的质心是一样的，那么相应的计数器加1
			if(flag) {
				counter = (Counter) context.getCounter("myCounter", "kmenasCounter");
				counter.increment(1l);
			}
			context.write(null, new Text(sb.toString()));
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = HdfsDAO.config();
		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		
		String kmeansPath = HDFS+"/kmeans/center.csv";
		String oldFile = HDFS+"/gplus/output/gplus_order/part-r-00000";
		String newFile = HDFS+"/kmeans/sample.csv";
		String resultPaht = HDFS+"/kmeans/output";
		
		hdfs.rmr(HDFS+"/kmeans/");
		hdfs.createFile(kmeansPath, "1 20\n30 40\n5000 2");
		hdfs.transformatFile(oldFile, newFile);
		Path kMeansPath = new Path(kmeansPath);	//初始的质心文件
		Path samplePath = new Path(newFile);	//样本文件
		//加载聚类中心文件
		Center center = new Center();
		String centerString = center.loadInitCenter(kMeansPath);
		
		
		int index = 0;	//迭代的次数
		while(index < 5) {
	
			conf.set(FLAG, centerString);	//将聚类中心的字符串放到configuration中
			
			kMeansPath = new Path(resultPaht + index);	//本次迭代的输出路径，也是下一次质心的读取路径
			
			/**判断输出路径是否存在，如果存在，则删除*/
		//	if(hdfs.exists(kMeansPath.toString())) hdfs.rmr(kMeansPath.toString());

			Job job = new Job(conf, "kmeans" + index); 
			job.setJarByClass(KmeansMR.class);
			job.setMapperClass(TokenizerMapper.class);
			job.setReducerClass(IntSumReducer.class);
			job.setOutputKeyClass(NullWritable.class);
			job.setOutputValueClass(Text.class);
			job.setMapOutputKeyClass(Text.class);
		    job.setMapOutputValueClass(Text.class);
		    FileInputFormat.addInputPath(job, samplePath);
		    FileOutputFormat.setOutputPath(job, kMeansPath);
			job.waitForCompletion(true);
			
			/**获取自定义counter的大小，如果等于质心的大小，说明质心已经不会发生变化了，则程序停止迭代*/
			long counter = job.getCounters().getGroup("myCounter").findCounter("kmenasCounter").getValue();
			if(counter == Center.k)	System.exit(0);
			/**重新加载质心*/
			center = new Center();
			centerString = center.loadCenter(kMeansPath);
		
			index ++;
		}
		System.exit(0);
	}
	
	public static double distance(double[] a, double[] b) {
		
		if(a == null || b == null || a.length != b.length) return Double.MAX_VALUE;
		double dis = 0;
		for(int i = 0; i < a.length; i++) {
			dis += Math.pow(a[i] - b[i], 2);
		}
		return Math.sqrt(dis);
	}
}	