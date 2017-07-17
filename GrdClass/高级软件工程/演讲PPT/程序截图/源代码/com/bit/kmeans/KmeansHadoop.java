package com.bit.kmeans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.conversion.InputDriver;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Vector;
import org.apache.mahout.utils.clustering.ClusterDumper;

import com.bit.hdfs.HdfsDAO;

public class KmeansHadoop {
	private static final String HDFS = "hdfs://master:9000";

	public static void main(String[] args) throws Exception {
		// String localFile = "datafile/randomData.csv";
		String localFile = "logfile/3.csv";
		String inPath = HDFS + "/test/hdfs/mix_data";
		String seqFile = inPath + "/seqfile";
		String seeds = inPath + "/seeds";
		String outPath = inPath + "/result/";
		String clusteredPoints = outPath + "/clusteredPoints";

		BufferedReader br = new BufferedReader(new FileReader(localFile));
		String data = br.readLine();// 一次读入一行，直到读入null为文件结束
		while (data != null) {
			String[] str = data.split("\t");
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
					"logfile/4.csv"), true));
			if (str.length == 2) {
				writer.write(str[0] + " " + str[1] + "\n");
				writer.flush();
			}

			data = br.readLine(); // 接着读下一行
		}
		localFile = "logfile/4.csv";
		JobConf conf = config();
		HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
		hdfs.rmr(inPath);
		hdfs.mkdirs(inPath);
		hdfs.copyFile(localFile, inPath);
		hdfs.ls(inPath);
		InputDriver.runJob(new Path(inPath), new Path(seqFile),
				"org.apache.mahout.math.RandomAccessSparseVector");

		int k = 1;
		Path seqFilePath = new Path(seqFile);
		Path clustersSeeds = new Path(seeds);
		DistanceMeasure measure = new EuclideanDistanceMeasure();
		clustersSeeds = RandomSeedGenerator.buildRandom(conf, seqFilePath,
				clustersSeeds, k, measure);
		KMeansDriver.run(conf, seqFilePath, clustersSeeds, new Path(outPath),
				measure, 0.01, 10, true, 0.01, false);

		Path outGlobPath = new Path(outPath, "clusters-*-final");
		Path clusteredPointsPath = new Path(clusteredPoints);
		System.out
				.printf("Dumping out clusters from clusters: %s and clusteredPoints: %s\n",
						outGlobPath, clusteredPointsPath);

		ClusterDumper clusterDumper = new ClusterDumper(outGlobPath,
				clusteredPointsPath);
		//displayCluster(clusterDumper);
		clusterDumper.printClusters(null);
	}

	public static JobConf config() {
		JobConf conf = new JobConf();
		conf.setJobName("ItemCFHadoop");
		conf.addResource("classpath:/hadoop/core-site.xml");
		conf.addResource("classpath:/hadoop/hdfs-site.xml");
		conf.addResource("classpath:/hadoop/mapred-site.xml");
		return conf;
	}

	public static void displayCluster(ClusterDumper clusterDumper) {
		Iterator<Integer> keys = clusterDumper.getClusterIdToPoints().keySet()
				.iterator();
		while (keys.hasNext()) {
			Integer center = keys.next();
			System.out.println("Center:" + center);
			for (WeightedVectorWritable point : clusterDumper
					.getClusterIdToPoints().get(center)) {
				Vector v = point.getVector();
				System.out.println(v.get(0) + "" + v.get(1));
			}
		}
	}
}
