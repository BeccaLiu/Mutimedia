package edu.usc.cs576.features;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.usc.cs576.AudioLoader;
import edu.usc.cs576.Complex;
import edu.usc.cs576.Constants;

public class AudioExtractor {
	// static int INTERVAL = 8;
	static int frequencyStart = 200;
	static int frequencyEnd = 2000;

	public static void main(String[] args) {
		File folder = new File("dataset");
		File[] files = folder.listFiles();
		for (File file : files) {
			String filename = file.getName();
			String ext = filename.substring(filename.length() - 4,
					filename.length());
			if (ext.equals(".wav")) {
				new AudioExtractor("dataset/" + filename);
			}
		}
	}

	public static void generateAudioFeature(int[][] frames, File outFile) {
		FileWriter fw;
		double sampleRate = Constants.FPS * frames[0].length;
		double[][] FFT = new double[frames.length][];
		FFT = generateFFT(frames); // generate fft[framenum][]
		double sampleInterval = sampleRate / FFT[0].length;
		int sampleStart = (int) (frequencyStart / sampleInterval);
		int sampleEnd = (int) (frequencyEnd / sampleInterval);
		int sampleCount = (int) Math
				.ceil((double) (sampleEnd - sampleStart) / 33);
		double[][] S = new double[frames.length][33];

		for (int i = 0; i < frames.length; i++) {
			for (int j = sampleStart; (j - sampleStart) < 33; j++) {
				for (int k = 1; k <= sampleCount; k++) {
					S[i][j - sampleStart] += FFT[i][j + k];
				}
			}
		}
		int[][] F = new int[frames.length][32];
		try {
			fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < frames.length - 1; i++) {
				for (int m = 0; m < 32; m++) {
					if (S[i + 1][m] - S[i + 1][m + 1] - S[i][m] + S[i][m + 1] > 0) {
						F[i][m] = 1;
						bw.write(F[i][m] + "\t");
					} else {
						F[i][m] = 0;
						bw.write(F[i][m] + "\t");
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

	static void generateFFTFile(double [][] fft, File outFile){
		try {
			FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(int i=0; i<fft.length; i++){
				for(int j=0; j<fft[0].length; j++){
					bw.write(fft[i][j]+"\t");
				}
				bw.write("\n");
			}
			
			bw.close();
			fw.close();
		}
		catch(Exception e){
			
		}
	}
	
	public static Complex[] fft(Complex[] x) {
		int N = x.length;
		// System.out.println(N);
		// base case
		if (N == 1)
			return new Complex[] { x[0] };

		// radix 2 Cooley-Tukey FFT
		if (N % 2 != 0) {
			throw new RuntimeException("N is not a power of 2");
		}

		// fft of even terms
		Complex[] even = new Complex[N / 2];
		for (int k = 0; k < N / 2; k++) {
			even[k] = x[2 * k];
		}
		Complex[] q = fft(even);

		// fft of odd terms
		Complex[] odd = even; // reuse the array
		for (int k = 0; k < N / 2; k++) {
			odd[k] = x[2 * k + 1];
		}
		Complex[] r = fft(odd);

		// combine
		Complex[] y = new Complex[N];
		for (int k = 0; k < N / 2; k++) {
			double kth = -2 * k * Math.PI / N;
			Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
			y[k] = q[k].plus(wk.times(r[k]));
			y[k + N / 2] = q[k].minus(wk.times(r[k]));
		}
		return y;
	}

	AudioExtractor(String filename) {
		File file = new File(filename);
		System.out.println(file);
		int[][] frames = AudioLoader.loadInt(file);
		File outFile = new File(filename + ".fp");
		generateAudioFeature(frames, outFile);
	}

	static double[][] generateFFT(int[][] input) {
		double[][] ret = new double[input.length][];
		for (int j = 0; j < input.length; j++) {
			int k = -1;
			int f = 0;
			do {
				k++;
				f = (int) Math.pow(2, k);

			} while (f < input[j].length);
			ret[j] = new double[(int) Math.pow(2, k)];
			for (int a = input[j].length; a < (int) Math.pow(2, k); a++) {
				ret[j][a] = 0;
			}
			Complex[] cinput = new Complex[(int) Math.pow(2, k)];
			for (int i = 0; i < input[j].length; i++) {
				cinput[i] = new Complex(input[j][i], 0);
			}
			for (int i = input[j].length; i < (int) Math.pow(2, k); i++) {
				cinput[i] = new Complex(0, 0);
			}

			Complex[] cy = fft(cinput);

			for (int i = 0; i < ret[j].length; i++) {
				ret[j][i] = cy[i].abs();
			}
		}
		return ret;
	}
}
