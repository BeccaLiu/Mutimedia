package edu.usc.cs576;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.SourceDataLine;

public class AudioLoader {
	
	public static int CUSTOM_FPS = 2;
	
	public static byte [][] load(File file){
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem
					.getAudioInputStream(new BufferedInputStream(
							new FileInputStream(file)));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		int totalFrames = (int) audioInputStream.getFrameLength();
		AudioFormat[] formats = info.getFormats();
		int bytesPerFrame = formats[0].getFrameSize();
		int totalVideoFrames = (int)(totalFrames/(formats[0].getSampleRate()/Constants.FPS));
		int EXTERNAL_BUFFER_SIZE = (int) (formats[0].getSampleRate()
				* bytesPerFrame / Constants.FPS);
		byte [][] bytes = new byte[totalVideoFrames][EXTERNAL_BUFFER_SIZE];
		byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
		try {
			for(int i=0; i<totalVideoFrames; i++){
				audioInputStream.read(audioBuffer , 0, audioBuffer.length);
				bytes[i] = audioBuffer.clone();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bytes;
	}
	
	public static int [][] loadInt(File file){
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem
					.getAudioInputStream(new BufferedInputStream(
							new FileInputStream(file)));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		int totalFrames = (int) audioInputStream.getFrameLength();
		AudioFormat[] formats = info.getFormats();
		int bytesPerFrame = formats[0].getFrameSize();
		int totalVideoFrames = (int)(totalFrames/(formats[0].getSampleRate()/CUSTOM_FPS));
		int EXTERNAL_BUFFER_SIZE = (int) (formats[0].getSampleRate()
				* bytesPerFrame / CUSTOM_FPS);
		byte [][] bytes = new byte[totalVideoFrames][EXTERNAL_BUFFER_SIZE];
		byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
		try {
			for(int i=0; i<totalVideoFrames; i++){
				audioInputStream.read(audioBuffer , 0, audioBuffer.length);
				bytes[i] = audioBuffer.clone();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int [][] ints = new int[totalVideoFrames][EXTERNAL_BUFFER_SIZE/bytesPerFrame];
		for(int i=0; i<totalVideoFrames; i++){
			for(int j=0; j<ints[0].length; j++){
				if(bytesPerFrame == 1){
					ints[i][j] = bytes[i][j] & 0xff;
				}
				else if(bytesPerFrame == 2){
					byte b1 = bytes[i][j*2];
					byte b2 = bytes[i][j*2+1];
					ints[i][j] = (b2 << 8) | (b1 & 0xff);
				}
				else if(bytesPerFrame == 3){
					byte b1 = bytes[i][j*3];
					byte b2 = bytes[i][j*3+1];
					byte b3 = bytes[i][j*3+2];
					ints[i][j] = (b3<<16) | (b2<<8 & 0xff00) | (b1 & 0xff);
				}
				else{
					// overflow
					int b1 = bytes[i][j*4];
					int b2 = bytes[i][j*4+1];
					int b3 = bytes[i][j*4+2];
					int b4 = bytes[i][j*4+3];
					ints[i][j] = (b4<<24) | (b3<<16 & 0xff0000) | (b2<<8 & 0xff00) | (b1 & 0xff);
				}
			}
		}
		
		return ints;
	}
}
