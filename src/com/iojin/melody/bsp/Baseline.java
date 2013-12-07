package com.iojin.melody.bsp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSPJob;

import com.iojin.melody.utils.FileUtil;
import com.iojin.melody.utils.TimerUtil;

public class Baseline {
	public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, InterruptedException {
		if (args.length != 11) {
			System.out.println("USAGE: <NUM_TASK> <PARAK> <DIMENSION> <NUM_BIN> <NUM_VECTOR> <INPUT_PATH> <BIN_PATH> <VECTOR_PATH> <OUTPUT_PATH> <CACHED> <BATCH>");
			return;
		}
		HamaConfiguration conf = new HamaConfiguration();
		conf.set("mapred.child.java.opts", "-Xmx512M");
		Path in = new Path(args[5]);
		Path out = new Path(args[8]);
		FileSystem fs = FileSystem.get(conf);
		if (fs.isFile(in)) {
			System.out.println("Input should be a directory");
			return;
		}
		
		FileUtil.deleteIfExistOnHDFS(conf, args[8]);
		FileUtil.addDependency(conf);
		DistributedCache.addCacheFile(new URI(args[6]),conf);
		DistributedCache.addCacheFile(new URI(args[7]),conf);
		
		conf.setInt(BaselineBSP.PARAK, Integer.valueOf(args[1]));
		conf.setInt(BaselineBSP.DIMENSION, Integer.valueOf(args[2]));
		conf.setInt(BaselineBSP.NUMBIN, Integer.valueOf(args[3]));
		conf.setInt(BaselineBSP.NUMVEC, Integer.valueOf(args[4]));
		conf.set(BaselineBSP.PATHIN, args[5]);
		conf.set(BaselineBSP.PATHBIN, args[6]);
		conf.set(BaselineBSP.PATHVEC, args[7]);
		conf.set(BaselineBSP.PATHOUT, args[8]);
		conf.setBoolean(BaselineBSP.CACHED, Boolean.valueOf(args[9]));
		conf.setInt(BaselineBSP.MSG_BATCH, Integer.valueOf(args[10]));
		conf.set("bsp.local.tasks.maximum", "" + Runtime.getRuntime().availableProcessors());
		
		BSPJob job = BaselineBSP.createJob(conf, in, out);
		job.setNumBspTask(Integer.valueOf(args[0]));
		TimerUtil.start();
		job.waitForCompletion(true);
		TimerUtil.end();
		TimerUtil.print();
	}
}
