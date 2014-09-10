package edu.usc.cs576;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import edu.usc.cs576.features.AudioExtractor;
import edu.usc.cs576.features.MatchScorer;
import edu.usc.cs576.features.VideoColorExtractor;
import edu.usc.cs576.features.VideoMotionExtractor;

public class AudioVideoQuery {

	static File videoFile;
	static File audioFile;

	byte [][] queryFrames;
	
	VideoPlayer queryPlayer;
	AudioPlayer queryAudioPlayer;
	VideoPlayer matchPlayer;
	AudioPlayer matchAudioPlayer;

	// sync control;
	long queryPlayTime;
	long queryDuration;
	long matchPlayTime;
	long matchDuration;

	protected Shell shell;
	private Label queryLabel;
	private Composite matchComposite;
	private Composite queryComposite;
	private Button queryPauseButton;
	private Button matchPauseButton;
	private Button queryStopButton;
	private Button queryPlayButton;
	private Button matchStopButton;
	private Button matchPlayButton;
	private Label lblMatchvideo;
	private List matchList;
	private Composite queryProgressBar;
	private Composite matchProgressBar;
	private Composite colorScoreComposite;
	private Composite motionScoreComposite;
	private Composite audioScoreComposite;
	private Composite totalScoreComposite;

	ArrayList<Integer> percentageResult;
	ArrayList<ArrayList<Integer>> colorScores;
	ArrayList<ArrayList<Integer>> motionScores;
	ArrayList<ArrayList<Integer>> audioScores;
	ArrayList<ArrayList<Integer>> totalScores;
	ArrayList<File> dataFilenames;
	
	ScoreShower colorScoreShower;
	ScoreShower motionScoreShower;
	ScoreShower audioScoreShower;
	ScoreShower totalScoreShower;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			String s = args[0];
			String ss = s.substring(s.length() - 3, s.length());
			if (ss.equals("wav")) {
				audioFile = new File(s);
			} else if (ss.equals("rgb")) {
				videoFile = new File(s);
			}
		} else if (args.length == 2) {
			for (int i = 0; i < 2; i++) {
				String s = args[i];
				String ss = s.substring(s.length() - 3, s.length());
				if (ss.equals("wav")) {
					audioFile = new File(s);
				} else if (ss.equals("rgb")) {
					videoFile = new File(s);
				}
			}
		}

		if(videoFile != null && !videoFile.exists()){
			videoFile = null;
			System.out.println("Video not found");
		}
		if(audioFile != null && !audioFile.exists()){
			audioFile = null;
			System.out.println("Audio not found");
		}
		
		AudioVideoQuery window = new AudioVideoQuery();
		window.open();
	}

	/**
	 * Open the window.
	 */
	public void open() {
		// set timer
		queryDuration = 0;
		matchDuration = 0;

		ArrayList<ArrayList<Integer>> queryColorFeature = null;
		ArrayList<ArrayList<Integer>> queryMotionFeature = null;
		ArrayList<ArrayList<Integer>> queryAudioFeature = null;
		if (videoFile != null) {
			queryFrames = VideoLoader.load(videoFile);
			
			// create feature file
			File colorFile = new File(videoFile.getAbsolutePath() + ".color");
			File motionFile = new File(videoFile.getAbsolutePath() + ".motion");
			if(!colorFile.exists())
				VideoColorExtractor.generateColorFeature(queryFrames, colorFile);
			if(!motionFile.exists())
				VideoMotionExtractor.generateMotionFeature(queryFrames, motionFile);
			
			queryColorFeature = MatchScorer.parseFile(colorFile);
			queryMotionFeature = MatchScorer.parseFile(motionFile);
		}else{
			// audio only
			if(audioFile != null){
				int[][] audioIntFrames = AudioLoader.loadInt(audioFile);
				// create feature file
				File fpFile = new File(audioFile.getAbsolutePath() + ".fp");
				if(!fpFile.exists()){
					AudioExtractor.generateAudioFeature(audioIntFrames, fpFile);
				}
				
				queryAudioFeature = MatchScorer.parseFile(fpFile);
			}
		}

		// do analysis
		percentageResult = new ArrayList<Integer>();
		colorScores = new ArrayList<ArrayList<Integer>>();
		motionScores = new ArrayList<ArrayList<Integer>>();
		audioScores = new ArrayList<ArrayList<Integer>>();
		totalScores = new ArrayList<ArrayList<Integer>>();
		dataFilenames = new ArrayList<File>();
		File dir = new File("dataset");
		for (File file : dir.listFiles()) {
			String filename = file.getName();
			String ext = filename.substring(filename.length() - 4,
					filename.length());
			
			ArrayList<Integer> colorScore = null;
			ArrayList<Integer> motionScore = null;
			ArrayList<Integer> audioScore = null;
			
			if (ext.equals(".rgb")) {
				if(videoFile != null){
					if(queryColorFeature != null){
						File colorFile = new File(file.getAbsolutePath() + ".color");
						ArrayList<ArrayList<Integer>> matchColorFeature = MatchScorer.parseFile(colorFile);
						
						colorScore = MatchScorer.getHistScores(queryColorFeature, matchColorFeature);
						int maxScore = Constants.IMAGE_HEIGHT*Constants.IMAGE_WIDTH*queryColorFeature.size();
						int max = -1;
						for(int i=0; i<colorScore.size(); i++){
							int s2 = colorScore.get(i)*100/maxScore;
							
							if(s2 < 0) s2 = 0;
							colorScore.set(i, s2);
							if(s2 > max)
								max = s2;
						}
						colorScores.add(colorScore);
					}
					if(queryMotionFeature != null){
						// compare motion feature
						File motionFile = new File(file.getAbsolutePath() + ".motion");
						ArrayList<ArrayList<Integer>> matchMotionFeature = MatchScorer.parseFile(motionFile);
						
						motionScore = MatchScorer.getScores(queryMotionFeature, matchMotionFeature);
						int maxError = VideoMotionExtractor.WORSTERROR*queryMotionFeature.size();
						
						int max = -1;
						for(int i=0; i<motionScore.size(); i++){
							int s2 = (maxError - motionScore.get(i))*100/maxError;
							if(s2 < 0) s2 = 0;
							motionScore.set(i, s2);
							if(s2 > max)
								max = s2;
						}
						motionScores.add(motionScore);
					}
					
					if(colorScore != null && motionScore != null){
						ArrayList<Integer> totalScore = MatchScorer.combineScore(colorScore, motionScore);
						int max = -1;
						for(int value:totalScore){
							if(value > max)
								max = value;
						}
						percentageResult.add(max);
						totalScores.add(totalScore);
					}
				}
				else{
					// audio only
					String fullPath = file.getAbsolutePath();
					String name = fullPath.substring(0,
							fullPath.length()-4) + ".wav";
					File matchAudioFile = new File(name);
					if(matchAudioFile.exists() && queryAudioFeature != null){
						File fpFile = new File(matchAudioFile.getAbsolutePath() + ".fp");
						ArrayList<ArrayList<Integer>> matchAudioFeature = MatchScorer.parseFile(fpFile);
						audioScore = MatchScorer.getScores(queryAudioFeature, matchAudioFeature);
						int maxScore = 32*queryAudioFeature.size();
						int max = -1;
						for(int i=0; i<audioScore.size(); i++){
							int s2 = (maxScore-audioScore.get(i))*100/maxScore;
							
							if(s2 < 0) s2 = 0;
							audioScore.set(i, s2);
							if(s2 > max)
								max = s2;
						}
						audioScores.add(audioScore);
						
						percentageResult.add(max);
//						totalScores.add(audioScore);
					}
				}
				dataFilenames.add(file);
			}
		}

		stupidSorting();
		
		Display display = Display.getDefault();
		createContents();

		showMatchList();

		queryPauseButton.setEnabled(false);
		matchPauseButton.setEnabled(false);

		shell.open();
		shell.layout();

		queryPlayer = new VideoPlayer(queryComposite, queryProgressBar);
		queryPlayer.start();
		if (videoFile == null)
			queryAudioPlayer = new AudioPlayer(queryProgressBar);
		else
			queryAudioPlayer = new AudioPlayer(null);
		queryAudioPlayer.start();

		matchPlayer = new VideoPlayer(matchComposite, matchProgressBar);
		matchPlayer.start();
		matchAudioPlayer = new AudioPlayer(null);
		matchAudioPlayer.start();

		initQueryPlayer();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		queryPlayer.dispose();
		matchPlayer.dispose();
		queryAudioPlayer.dispose();
		matchAudioPlayer.dispose();
	}
	
	void stupidSorting(){
		// selection sort
		for(int i=0; i<percentageResult.size(); i++){
			int max = Integer.MIN_VALUE;
			int maxAt = -1;
			for(int j=i; j<percentageResult.size(); j++){
				int v = percentageResult.get(j);
				
				if(v > max){
					max = v;
					maxAt = j;
				}
			}
			
			// swap i and maxAt
			swap(percentageResult, i, maxAt);
			swap(colorScores, i, maxAt);
			swap(motionScores, i, maxAt);
			swap(audioScores, i, maxAt);
			swap(totalScores, i, maxAt);
			swap(dataFilenames, i, maxAt);
		}
	}
	
	void swap(ArrayList list, int i, int j){
		if(list == null || list.size() == 0)
			return;
		Object temp = list.get(i);
		list.set(i, list.get(j));
		list.set(j, temp);
	}

	void showMatchList() {
		// should change later
		for (int i = 0; i < percentageResult.size(); i++) {
			String filename = dataFilenames.get(i).getName();
			matchList.add(String.format("%3d%% :  %s", percentageResult.get(i),
					filename));
		}
	}

	void initQueryPlayer() {
		queryDuration = 0;

		if (videoFile != null) {
			byte[][] queryFrames = VideoLoader.load(videoFile);
			queryPlayer.loadVideo(queryFrames);
		}
		if (audioFile != null) {
			byte [][] audios = AudioLoader.load(audioFile);
			queryAudioPlayer.loadAudio(audioFile, audios);
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(760, 571+120);
		shell.setText("SWT Application");

		queryLabel = new Label(shell, SWT.NONE);
		queryLabel.setBounds(10, 10, 208, 15);
		queryLabel.setText("Query");

		matchList = new List(shell, SWT.V_SCROLL | SWT.BORDER);
		matchList.setBounds(382, 31, 352, 86);
		matchList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// not used
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (!matchPlayButton.getEnabled()) {
					matchPauseButton.setEnabled(false);
					matchPlayButton.setEnabled(true);
					if (matchPlayer != null) {
						matchPlayer.stopVideo();
					}
					if (matchAudioPlayer != null) {
						matchAudioPlayer.stopAudio();
					}
				}
				matchDuration = 0;

				int ind = matchList.getSelectionIndex();
				byte[][] matchFrames = VideoLoader.load(dataFilenames.get(ind));
				matchPlayer.loadVideo(matchFrames);

				StringBuilder filename = new StringBuilder(dataFilenames.get(
						ind).getName());
				filename.delete(filename.length() - 3, filename.length());
				filename.append("wav");

				File audioFile = new File("dataset/" + filename.toString());
				if (audioFile.exists()) {
					byte [][] audios = AudioLoader.load(audioFile);
					matchAudioPlayer.loadAudio(audioFile, audios);
				}
				else{
					matchAudioPlayer.loadAudio(null, null);
				}
				
				// score shower
				if(colorScoreShower == null)
					colorScoreShower = new ScoreShower(colorScoreComposite, new Color(Display.getCurrent(), 255, 0, 0));
				if(motionScoreShower == null)
					motionScoreShower = new ScoreShower(motionScoreComposite, new Color(Display.getCurrent(), 0, 255, 0));
				if(audioScoreShower == null){
					audioScoreShower = new ScoreShower(audioScoreComposite, new Color(Display.getCurrent(), 0, 0, 255));
				}
				if(totalScoreShower == null)
					totalScoreShower = new ScoreShower(totalScoreComposite, new Color(Display.getCurrent(), 100, 100, 100));
				
				if(colorScores.size() != 0)
					colorScoreShower.draw(colorScores.get(ind), matchFrames.length);
				if(motionScores.size() != 0)
					motionScoreShower.draw(motionScores.get(ind), matchFrames.length);
				if(audioScores.size() != 0)
					audioScoreShower.draw_audio(audioScores.get(ind), matchFrames.length);
				if(totalScores.size() != 0)
					totalScoreShower.draw(totalScores.get(ind), matchFrames.length);
			}
		});

		queryComposite = new Composite(shell, SWT.NONE);
		queryComposite.setBounds(10, 183+120, 352, 288);

		matchComposite = new Composite(shell, SWT.NONE);
		matchComposite.setBounds(382, 183+120, 352, 288);

		queryPlayButton = new Button(shell, SWT.NONE);
		queryPlayButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				queryPauseButton.setEnabled(true);
				queryPlayButton.setEnabled(false);
				queryPlayTime = System.currentTimeMillis();
				if (queryPlayer != null) {
					queryPlayer.playVideo(queryPlayTime, queryDuration);
				}
				if (queryAudioPlayer != null) {
					queryAudioPlayer.playAudio(queryPlayTime, queryDuration);
				}
			}
		});
		queryPlayButton.setBounds(10, 498+120, 75, 25);
		queryPlayButton.setText("Play");

		queryStopButton = new Button(shell, SWT.NONE);
		queryStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				queryPauseButton.setEnabled(false);
				queryPlayButton.setEnabled(true);
				queryDuration = 0;
				if (queryPlayer != null) {
					queryPlayer.stopVideo();
				}
				if (queryAudioPlayer != null) {
					queryAudioPlayer.stopAudio();
				}
			}
		});
		queryStopButton.setBounds(172, 498+120, 75, 25);
		queryStopButton.setText("Stop");

		matchProgressBar = new Composite(shell, SWT.NONE);
		matchProgressBar.setBounds(382, 162+120, 352, 15);

		matchPlayButton = new Button(shell, SWT.NONE);
		matchPlayButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				matchPauseButton.setEnabled(true);
				matchPlayButton.setEnabled(false);
				matchPlayTime = System.currentTimeMillis();
				if (matchPlayer != null) {
					matchPlayer.playVideo(matchPlayTime, matchDuration);
				}
				if (matchAudioPlayer != null) {
					matchAudioPlayer.playAudio(matchPlayTime, matchDuration);
				}
			}
		});
		matchPlayButton.setBounds(382, 498+120, 75, 25);
		matchPlayButton.setText("Play");

		matchPauseButton = new Button(shell, SWT.NONE);
		matchPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				matchPauseButton.setEnabled(false);
				matchPlayButton.setEnabled(true);
				matchDuration += System.currentTimeMillis() - matchPlayTime;
				if (matchPlayer != null) {
					matchPlayer.pauseVideo();
				}
				if (matchAudioPlayer != null) {
					matchAudioPlayer.pauseAudio();
				}
			}
		});
		matchPauseButton.setBounds(463, 498+120, 75, 25);
		matchPauseButton.setText("Pause");

		matchStopButton = new Button(shell, SWT.NONE);
		matchStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				matchPauseButton.setEnabled(false);
				matchPlayButton.setEnabled(true);
				matchDuration = 0;
				if (matchPlayer != null) {
					matchPlayer.stopVideo();
				}
				if (matchAudioPlayer != null) {
					matchAudioPlayer.stopAudio();
				}
			}
		});
		matchStopButton.setBounds(544, 498+120, 75, 25);
		matchStopButton.setText("Stop");

		queryPauseButton = new Button(shell, SWT.NONE);
		queryPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				queryPauseButton.setEnabled(false);
				queryPlayButton.setEnabled(true);
				queryDuration += System.currentTimeMillis() - queryPlayTime;
				if (queryPlayer != null) {
					queryPlayer.pauseVideo();
				}
				if (queryAudioPlayer != null) {
					queryAudioPlayer.pauseAudio();
				}
			}
		});
		queryPauseButton.setBounds(91, 498+120, 75, 25);
		queryPauseButton.setText("Pause");

		lblMatchvideo = new Label(shell, SWT.NONE);
		lblMatchvideo.setBounds(382, 10, 75, 15);
		lblMatchvideo.setText("MatchVideos");

		queryProgressBar = new Composite(shell, SWT.NONE);
		queryProgressBar.setBounds(10, 162+120, 352, 15);
		
		colorScoreComposite = new Composite(shell, SWT.NONE);
		colorScoreComposite.setBounds(382, 162, 352, 20);
		
		motionScoreComposite = new Composite(shell, SWT.NONE);
		motionScoreComposite.setBounds(382, 162+30, 352, 20);
		
		audioScoreComposite = new Composite(shell, SWT.NONE);
		audioScoreComposite.setBounds(382, 162+60, 352, 20);
		
		totalScoreComposite = new Composite(shell, SWT.NONE);
		totalScoreComposite.setBounds(382, 162+90, 352, 20);

	}

	public Label getQueryLabel() {
		return queryLabel;
	}

	public Composite getMatchComposite() {
		return matchComposite;
	}

	public Composite getQueryComposite() {
		return queryComposite;
	}

	public Button getQueryPauseButton() {
		return queryPauseButton;
	}

	public Button getMatchPauseButton() {
		return matchPauseButton;
	}

	public Composite getMatchProgressBar() {
		return matchProgressBar;
	}

	public Button getQueryStopButton() {
		return queryStopButton;
	}

	public Button getQueryPlayButton() {
		return queryPlayButton;
	}

	public Button getMatchStopButton() {
		return matchStopButton;
	}

	public Button getMatchPlayButton() {
		return matchPlayButton;
	}

	public Label getLblMatchvideo() {
		return lblMatchvideo;
	}

	public List getMatchList() {
		return matchList;
	}

	public Composite getQueryProgressBar() {
		return queryProgressBar;
	}
}
