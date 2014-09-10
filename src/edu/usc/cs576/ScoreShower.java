package edu.usc.cs576;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ScoreShower {
	GC gc;
	Color color;
	public ScoreShower(Composite composite, Color color) {
		gc = new GC(composite);
		this.color = color;
	}
	
	void draw(ArrayList<Integer> score, int frameN){
		gc.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		gc.fillRectangle(0, 0, Constants.IMAGE_WIDTH, Constants.SCORESHOWER_HEIGHT);
		
		gc.setBackground(color);
		
		for(int i=0; i<Constants.IMAGE_WIDTH; i++){
			int f = i*frameN/Constants.IMAGE_WIDTH;
			if(f < 0 || f >= score.size())
				continue;
			
			int s = score.get(f);
			int h = s*Constants.SCORESHOWER_HEIGHT/100;
			
			gc.fillRectangle(i, Constants.SCORESHOWER_HEIGHT - h, 1, h);
		}
	}
	
	void draw_audio(ArrayList<Integer> score, int frameN){
		
		double ratio = Constants.FPS;
		ratio = ratio/AudioLoader.CUSTOM_FPS;
		
		gc.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		gc.fillRectangle(0, 0, Constants.IMAGE_WIDTH, Constants.SCORESHOWER_HEIGHT);
		
		gc.setBackground(color);
		
		for(int i=0; i<Constants.IMAGE_WIDTH; i++){
			int f = (int)(((double)(i)/ratio*frameN/Constants.IMAGE_WIDTH));
			if(f < 0 || f >= score.size())
				continue;
			
			int s = score.get(f);
			int h = s*Constants.SCORESHOWER_HEIGHT/100;
			
			gc.fillRectangle(i, Constants.SCORESHOWER_HEIGHT - h, 1, h);
		}
	}
}
