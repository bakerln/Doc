package com.bit.kmeans;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

public class Center {

	protected static int k = 3;		//质心的个数
	
	/**
	 * 从初始的质心文件中加载质心，并返回字符串，质心之间用tab分割
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public String loadInitCenter(Path path) throws IOException {
		
		StringBuffer sb = new StringBuffer();
		
		Configuration conf = new Configuration();
		FileSystem hdfs = FileSystem.get(URI.create(path.toString()), conf);
		FSDataInputStream dis = hdfs.open(path);
		LineReader in = new LineReader(dis, conf);
		Text line = new Text();
		while(in.readLine(line) > 0) {
			
			sb.append(line.toString().trim());
			sb.append("\t");
		}
		
		return sb.toString().trim();
	}
	
	/**
	 * 从每次迭代的质心文件中读取质心，并返回字符串
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public String loadCenter(Path path) throws IOException {
		
		StringBuffer sb = new StringBuffer();
		
		Configuration conf = new Configuration();
		FileSystem hdfs = FileSystem.get(URI.create(path.toString()), conf);
		FileStatus[] files = hdfs.listStatus(path);
		
		for(int i = 0; i < files.length; i++) {
			
			Path filePath = files[i].getPath();
			if(!filePath.getName().contains("part")) continue;
			FSDataInputStream dis = hdfs.open(filePath);
			LineReader in = new LineReader(dis, conf);
			Text line = new Text();
			while(in.readLine(line) > 0) {
				sb.append(line.toString().trim());
				sb.append("\t");
			}
		}
		//System.out.println(sb.toString().trim());
		return sb.toString().trim();
	}
}
