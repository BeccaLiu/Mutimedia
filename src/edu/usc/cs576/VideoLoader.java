package edu.usc.cs576;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class VideoLoader {
	public static byte [][] load(File file){
		try {
			InputStream is = new FileInputStream(file);
			int frameN = (int) (file.length() / Constants.IMAGE_HEIGHT / Constants.IMAGE_WIDTH) / 3;
			byte [][] frames = new byte[frameN][Constants.IMAGE_WIDTH*Constants.IMAGE_HEIGHT*3];
			for (int i = 0; i < frameN; i++) {
				byte[] bytes = new byte[Constants.IMAGE_HEIGHT
						* Constants.IMAGE_WIDTH * 3];

				int numRead = 0;
				int offset = 0;
				while (offset < bytes.length
						&& (numRead = is.read(bytes, offset, bytes.length
								- offset)) >= 0) {
					offset += numRead;
				}

				bytes = reorder(bytes);

				frames[i] = bytes;
			}
			is.close();
			return frames;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] reorder(byte[] data) {
		byte[] newData = new byte[data.length];
		int ind = 0;
		for (int y = 0; y < Constants.IMAGE_HEIGHT; y++) {

			for (int x = 0; x < Constants.IMAGE_WIDTH; x++) {

				byte r = data[ind];
				byte g = data[ind + Constants.IMAGE_HEIGHT
						* Constants.IMAGE_WIDTH];
				byte b = data[ind + Constants.IMAGE_HEIGHT
						* Constants.IMAGE_WIDTH * 2];

				newData[ind * 3] = r;
				newData[ind * 3 + 1] = g;
				newData[ind * 3 + 2] = b;
				ind++;
			}
		}
		return newData;
	}
	
	public static byte[][] toYUVFrames(byte [][] frames){
		byte [][] yuvFrames = new byte[frames.length][frames[0].length];
		for(int i=0; i<frames.length; i++){
			for(int x=0; x<Constants.IMAGE_WIDTH; x++){
				for(int y=0; y<Constants.IMAGE_HEIGHT; y++){
					double r = getR(frames, i, x, y);
					double g = getG(frames, i, x, y);
					double b = getB(frames, i, x, y);
					
					double Y = 0.299*r + 0.587*g + 0.114*b;
					double U = (-0.14713*r -0.2886*g + 0.436*b)/0.436;
					double V = (0.615*r -0.51499*g -0.10001*b)/0.615;
					
					if(Y > 255) Y = 255;
					if(Y < 0) Y = 0;
					
					if(U > 255) U = 255;
					if(U < 0) U = 0;
					
					if(V > 255) V = 255;
					if(V < 0) V = 0;
					
					int Y2 = (int) Y;
					int U2 = (int) U;
					int V2 = (int) V;
					
					byte [] yuvFrame = yuvFrames[i];
					yuvFrame[(y*Constants.IMAGE_WIDTH + x)*3] = (byte) Y2;
					yuvFrame[(y*Constants.IMAGE_WIDTH + x)*3 + 1] = (byte) U2;
					yuvFrame[(y*Constants.IMAGE_WIDTH + x)*3 + 2] = (byte) V2;
				}
			}
		}
		return yuvFrames;
	}
	
	public static int getR(byte [][] frames, int i, int x, int y){
		byte [] frame = frames[i];
		return frame[(y*Constants.IMAGE_WIDTH + x)*3] & 0xff;
	}
	
	public static int getG(byte [][] frames, int i, int x, int y){
		byte [] frame = frames[i];
		return frame[(y*Constants.IMAGE_WIDTH + x)*3 + 1] & 0xff;
	}
	
	public static int getB(byte [][] frames, int i, int x, int y){
		byte [] frame = frames[i];
		return frame[(y*Constants.IMAGE_WIDTH + x)*3 + 2] & 0xff;
	}
	
//	public static int getY(byte [][] frames, int i, int x, int y){
//		double r = getR(frames, i, x, y);
//		double g = getG(frames, i, x, y);
//		double b = getB(frames, i, x, y);
//		
//		double ret = 0.299*r + 0.587*g + 0.114*b;
//		if(ret > 255) ret = 255;
//		if(ret < 0) ret = 0;
//		return (int) ret;
//	}
//	
//	public static int getU(byte [][] frames, int i, int x, int y){
//		double r = getR(frames, i, x, y);
//		double g = getG(frames, i, x, y);
//		double b = getB(frames, i, x, y);
//		
//		double ret = (-0.14713*r -0.2886*g + 0.436*b)/0.436;
//		if(ret > 255) ret = 255;
//		if(ret < 0) ret = 0;
//		return (int) ret;
//	}
//	
//	public static int getV(byte [][] frames, int i, int x, int y){
//		double r = getR(frames, i, x, y);
//		double g = getG(frames, i, x, y);
//		double b = getB(frames, i, x, y);
//		
//		double ret = (0.615*r -0.51499*g -0.10001*b)/0.615;
//		if(ret > 255) ret = 255;
//		if(ret < 0) ret = 0;
//		return (int) ret;
//	}
}
