package com.bit.gplus.combined;

import java.io.IOException;
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
    static JobConf conf = config();
    static HdfsDAO hdfs = new HdfsDAO(HDFS, conf);
    public static void main(String[] args) throws Exception {
//        String localFile = "datafile/randomData.csv";
        String inPath = HDFS + "/gplus/kmeans";
        String seqFile = inPath + "/seqfile";
        String seeds = inPath + "/seeds";
        String outPath = inPath + "/result/";
        String clusteredPoints = outPath + "/clusteredPoints";
        String oldFile = HDFS+"/gplus/output/gplus_order/part-r-00000";
        

        
        hdfs.rmr(inPath);
        hdfs.transformatFile(oldFile, inPath+"/1.csv");
        hdfs.ls(inPath);

        InputDriver.runJob(new Path(inPath), new Path(seqFile), "org.apache.mahout.math.RandomAccessSparseVector");

        int k = 3;
        Path seqFilePath = new Path(seqFile);
        Path clustersSeeds = new Path(seeds);
        DistanceMeasure measure = new EuclideanDistanceMeasure();
        clustersSeeds = RandomSeedGenerator.buildRandom(conf, seqFilePath, clustersSeeds, k, measure);
        KMeansDriver.run(conf, seqFilePath, clustersSeeds, new Path(outPath), measure, 0.01, 10, true, 0.01, false);

        Path outGlobPath = new Path(outPath, "clusters-*-final");
        Path clusteredPointsPath = new Path(clusteredPoints);
        System.out.printf("Dumping out clusters from clusters: %s and clusteredPoints: %s\n", outGlobPath, clusteredPointsPath);

        ClusterDumper clusterDumper = new ClusterDumper(outGlobPath, clusteredPointsPath);
        displayCluster(clusterDumper);
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
        Iterator<Integer> keys = clusterDumper.getClusterIdToPoints().keySet().iterator();
        while (keys.hasNext()) {
            Integer center = keys.next();
        	String string = new String();
            System.out.println("Center:" + center);
            for (WeightedVectorWritable point : clusterDumper.getClusterIdToPoints().get(center)) {
                Vector v = point.getVector();
                System.out.println(v.get(0) + "" + v.get(1));
            	string += (int)v.get(0) + "\t" + (int)v.get(1) + "\n";
            }
        	try {
				hdfs.createFile(HDFS + "/gplus/kmeans/center/" + center
						+ ".csv", string);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
