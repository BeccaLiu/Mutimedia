package edu.usc.cs576.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class MatchScorer {
	
	public static void main(String [] args){
		File sFile = new File(args[0]);
		File mFile = new File(args[1]);
		
		ArrayList<ArrayList<Integer>> queryFeature = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> matchFeature = new ArrayList<ArrayList<Integer>>();
		
		queryFeature = parseFile(sFile);
		matchFeature = parseFile(mFile);
		
		int [] score = new int[matchFeature.size() - queryFeature.size()+1];
		for(int i=0; i<score.length; i++){
			int sc = videoScoreAt(queryFeature, matchFeature, i);
			score[i] = sc;
			System.out.println(sc);
		}
	}
	
	public static ArrayList<ArrayList<Integer>> parseFile(File file){
		ArrayList<ArrayList<Integer>> array = new ArrayList<ArrayList<Integer>>();
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(file));
			while ((sCurrentLine = br.readLine()) != null) {
				ArrayList<Integer> ret = new ArrayList<Integer>();
				Scanner sc = new Scanner(sCurrentLine);
				while(sc.hasNext()){
					int val = sc.nextInt();
					ret.add(val);
				}
				if(ret.size() > 0)
					array.add(ret);
				sc.close();
			}
			br.close();
		} catch (Exception e) {
		}
		return array;
	}
	
	public static ArrayList<Integer> getScores(ArrayList<ArrayList<Integer>>queryFeature, ArrayList<ArrayList<Integer>> matchFeature){
		ArrayList<Integer> score = new ArrayList<Integer>();
		for(int i=0; i<matchFeature.size() - queryFeature.size()+1; i++){
			int sc = videoScoreAt(queryFeature, matchFeature, i);
			score.add(sc);
		}
		return score;
	}
	
	public static ArrayList<Integer> getHistScores(ArrayList<ArrayList<Integer>>queryFeature, ArrayList<ArrayList<Integer>> matchFeature){
		ArrayList<Integer> score = new ArrayList<Integer>();
		for(int i=0; i<matchFeature.size() - queryFeature.size()+1; i++){
			int sc = videoHistScoreAt(queryFeature, matchFeature, i);
			score.add(sc);
		}
		return score;
	}
	
	static int videoScoreAt(ArrayList<ArrayList<Integer>>queryFeature, ArrayList<ArrayList<Integer>> matchFeature, int offset){
		int dif = 0;
		int m = queryFeature.size();
		for(int i=0; i<m; i++){
			ArrayList<Integer> f1 = queryFeature.get(i);
			ArrayList<Integer> f2 = matchFeature.get(i+offset);
			dif += featureScore(f1, f2);
		}
		return dif;
	}
	
	static int videoHistScoreAt(ArrayList<ArrayList<Integer>>queryFeature, ArrayList<ArrayList<Integer>> matchFeature, int offset){
		int dif = 0;
		int m = queryFeature.size();
		for(int i=0; i<m; i++){
			ArrayList<Integer> f1 = queryFeature.get(i);
			ArrayList<Integer> f2 = matchFeature.get(i+offset);
			dif += featureHistScore(f1, f2);
		}
		return dif;
	}
	
	static int featureScore(ArrayList<Integer> f1, ArrayList<Integer> f2){
		int sum = 0;
		for(int i=0; i<f1.size(); i++){
			sum += Math.abs(f1.get(i)-f2.get(i));
		}
		return sum;
	}
	
	static int featureHistScore(ArrayList<Integer> f1, ArrayList<Integer> f2){
		int sum = 0;
		for(int i=0; i<f1.size(); i++){
			sum += Math.min(f1.get(i), f2.get(i));
		}
		return sum;
	}
	
	public static ArrayList<Integer> combineScore(ArrayList<Integer> s1, ArrayList<Integer> s2){
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for(int i=0; i<s1.size(); i++){
			int v1 = s1.get(i);
			int v2 = s2.get(i);
			int s = (v1*1 + v2*1)/2;
			ret.add(s);
		}
		return ret;
	}
}
