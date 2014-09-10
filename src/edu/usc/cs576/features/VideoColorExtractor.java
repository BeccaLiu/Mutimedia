package edu.usc.cs576.features;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.usc.cs576.Constants;
import edu.usc.cs576.VideoLoader;


public class VideoColorExtractor {
	static int RBIN = 6;
	static int GBIN = 6;
	static int BBIN = 6;
	
	public static void main(String [] args){
		File folder = new File("dataset");
		File [] files = folder.listFiles();
		for(File file:files){
			String filename = file.getName();
			String ext = filename.substring(filename.length()-4, filename.length());
			if(ext.equals(".rgb")){
				new VideoColorExtractor("dataset/"+filename);
			}
		}
		
		
	}
	
	public static void generateColorFeature(byte [][] frames, File outFile){
		FileWriter fw;
		int his[][][] = new int[RBIN][GBIN][BBIN];
		
		try {
			fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < frames.length-1; i++) {
				for(int l= 0; l<RBIN; l++){
					for(int j= 0; j<GBIN; j++){
						for(int k= 0; k<BBIN; k++){
							his[l][j][k]=0;					
						}				
					}
				}
				for (int y = 0; y < (Constants.IMAGE_HEIGHT); y = y + 1) {
					for (int x = 0; x < (Constants.IMAGE_WIDTH); x = x + 1) {
						int Y = VideoLoader.getR(frames, i, x, y);
						int U  = VideoLoader.getG(frames, i, x, y);
						int V = VideoLoader.getB(frames, i, x, y);
					    
						int yi = Y * RBIN / 255;
						int ui = U * GBIN / 255;
						int vi = V * BBIN / 255;
						
						if(yi >= RBIN) yi = RBIN-1;
						if(ui >= GBIN) ui = GBIN-1;
						if(vi >= BBIN) vi = BBIN-1;
						
						his[yi][ui][vi] += 1;					
						
					}
					
				}
				for(int r= 0; r<RBIN; r++){
					for(int g= 0;g<GBIN; g++){
						for(int b= 0; b<BBIN; b++){
							bw.write(his[r][g][b] + "\t");					
						}				
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
	
	VideoColorExtractor(String filename){
		File file = new File(filename);
		System.out.println(file);
		byte [][] frames = VideoLoader.load(file);
		File outFile = new File(filename + ".color");
		
		generateColorFeature(frames, outFile);
	}
}
