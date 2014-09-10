package edu.usc.cs576.features;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.usc.cs576.Constants;
import edu.usc.cs576.VideoLoader;

public class VideoMotionExtractor {

	static int GRID = 16;
	static int STEP = 4;
	static int K = 16;
	
	public static int WORSTERROR = 26*20*16;

	public static void main(String[] args) {
//		if (args.length == 1) {
//			new VideoMotionExtractor(args[0]);
//		}
		// extract all from dataset
		File folder = new File("dataset");
		File [] files = folder.listFiles();
		for(File file:files){
			String filename = file.getName();
			String ext = filename.substring(filename.length()-4, filename.length());
			if(ext.equals(".rgb")){
				new VideoMotionExtractor("dataset/"+filename);
			}
		}
	}

	public VideoMotionExtractor(String filename) {
		File file = new File(filename);
		System.out.println(file);
		byte [][] frames = VideoLoader.load(file);
		long start = System.currentTimeMillis();
		File outFile = new File(filename + ".motion");
		generateMotionFeature(frames, outFile);
		System.out.println(System.currentTimeMillis()-start);
	}
	
	public static void generateMotionFeature(byte [][] frames, File outFile){
		FileWriter fw;
		try {
			
			// use YUV frame
			frames = VideoLoader.toYUVFrames(frames);
			
			fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < frames.length-1; i++) {
				for (int y = GRID; y < (Constants.IMAGE_HEIGHT-GRID); y = y + GRID) {
					for (int x = GRID; x < (Constants.IMAGE_WIDTH-GRID); x = x + GRID) {
						int motion = getMotion(frames, i, x, y);
						bw.write(motion + "\t");
					}
				}
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static int getMotion(byte [][] frames, int i, int x, int y) {

		if (i == (frames.length - 1))
			return 0;

		// full search
		// int minError = Integer.MAX_VALUE;
		// int minX2 = -1;
		// int minY2 = -1;
		//
		// for(int x2 = x-K; x2 <= x+K; x2++){
		// for(int y2 = y-K; y2 <= y+K; y2++){
		// if(x2 < 0 || (x2+GRID) >= Constants.IMAGE_WIDTH || y2 < 0 ||
		// (y2+GRID) >= Constants.IMAGE_HEIGHT)
		// continue;
		// int dif = getDiff(i, i+1, x, y, x2, y2);
		// if(dif >= 0 && dif < minError){
		// minError = dif;
		// minX2 = x2;
		// minY2 = y2;
		// }
		// }
		// }

		// pyrid search
		int minError = Integer.MAX_VALUE;
		int minX2 = -1;
		int minY2 = -1;

		// init search
		for (int x2 = x - K; x2 <= x + K; x2 = x2 + STEP) {
			for (int y2 = y - K; y2 <= y + K; y2 = y2 + STEP) {
				if (x2 < 0 || (x2 + GRID) >= Constants.IMAGE_WIDTH || y2 < 0
						|| (y2 + GRID) >= Constants.IMAGE_HEIGHT)
					continue;
				int dif = getDiff(frames, i, i + 1, x, y, x2, y2);
				if (dif >= 0 && dif < minError) {
					minError = dif;
					minX2 = x2;
					minY2 = y2;
				}
			}
		}
		
		int step = STEP / 2;
		
		while (step > 0) {
			int xl = minX2-step;
			int xh = minX2+step;
			int yl = minY2-step;
			int yh = minY2+step;
			
			for (int x2 = xl; x2<=xh; x2=x2+step) {
				for (int y2=yl; y2<=yh; y2=y2+step) {
					if (x2 < 0 || (x2 + GRID) >= Constants.IMAGE_WIDTH || y2 < 0
							|| (y2 + GRID) >= Constants.IMAGE_HEIGHT)
						continue;
					
					int dif = getDiff(frames, i, i + 1, x, y, x2, y2);
					if (dif >= 0 && dif < minError) {
						minError = dif;
						minX2 = x2;
						minY2 = y2;
					}
				}
			}
			step = step / 2;
		}

		return (int) Math.sqrt((x - minX2) * (x - minX2) + (y - minY2)
				* (y - minY2));
	}

	static int getDiff(byte [][] frames, int i, int j, int x, int y, int x2, int y2) {
		int dif = 0;
		for (int itX = 0; itX < GRID; itX++) {
			for (int itY = 0; itY < GRID; itY++) {
				int ix = x + itX;
				int ix2 = x2 + itX;
				int iy = y + itY;
				int iy2 = y2 + itY;

				if (ix < 0 || ix >= Constants.IMAGE_WIDTH)
					continue;
				if (ix2 < 0 || ix2 >= Constants.IMAGE_WIDTH)
					continue;
				if (iy < 0 || iy >= Constants.IMAGE_HEIGHT)
					continue;
				if (iy2 < 0 || iy2 >= Constants.IMAGE_HEIGHT)
					continue;

//				int r1 = VideoLoader.getR(frames, i, ix, iy);
//				int g1 = VideoLoader.getG(frames, i, ix, iy);
//				int b1 = VideoLoader.getB(frames, i, ix, iy);
//
//				int r2 = VideoLoader.getR(frames, j, ix2, iy2);
//				int g2 = VideoLoader.getG(frames, j, ix2, iy2);
//				int b2 = VideoLoader.getB(frames, j, ix2, iy2);
//
//				dif += Math.abs(r1 - r2);
//				dif += Math.abs(g1 - g2);
//				dif += Math.abs(b1 - b2);
				
				int Y1 = VideoLoader.getR(frames, i, ix, iy);
				int Y2 = VideoLoader.getR(frames, j, ix2, iy2);
				
				dif += Math.abs(Y1-Y2);
			}
		}
		return dif;
	}
}
