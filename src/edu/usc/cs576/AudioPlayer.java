package edu.usc.cs576;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class AudioPlayer extends Thread {
	// for audio only
	Composite progressBar;
	GC gcProgress;

	long playTime;
	long duration;

	boolean asynPlay;
	boolean asynPause;
	boolean asynStop;
	boolean asynLoad;
	boolean asynDispose;
	boolean dispose;

	File f;
	byte[][] audios;
	int EXTERNAL_BUFFER_SIZE = 3528; // equals to 1 frame
	SourceDataLine dataLine;

	int totalVideoFrames;
	int currentVideoFrame;
	int bytesPerFrame;
	float sampleRate;

	public AudioPlayer(Composite progressBar) {
		this.progressBar = progressBar;
		if (progressBar != null)
			gcProgress = new GC(progressBar);

		playTime = 0;
		duration = 0;

		asynPlay = false;
		asynPause = false;
		asynStop = false;
		asynLoad = false;
		dispose = false;
	}

	void initPlayer() {
		if (f == null)
			return;
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem
					.getAudioInputStream(new BufferedInputStream(
							new FileInputStream(f)));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		int totalFrames = (int) audioInputStream.getFrameLength();
		AudioFormat[] formats = info.getFormats();
		sampleRate = formats[0].getSampleRate();
		bytesPerFrame = formats[0].getFrameSize();
		EXTERNAL_BUFFER_SIZE = (int) (formats[0].getSampleRate()
				* bytesPerFrame / Constants.FPS);

		currentVideoFrame = 0;
		totalVideoFrames = (int)(totalFrames/(formats[0].getSampleRate()/Constants.FPS));
		
		// opens the audio channel
		dataLine = null;
		try {
			dataLine = (SourceDataLine) AudioSystem.getLine(info);
			dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Starts the music :P
		dataLine.start();

		if (progressBar != null) {
			gcProgress.setBackground(new Color(Display.getCurrent(), 255, 255,
					255));
			gcProgress.fillRectangle(0, 0, Constants.IMAGE_WIDTH,
					Constants.PROGRESSBAR_HEIGHT);
		}
	}

	@Override
	public void run() {
		int state = 0;
		double interFrameTime = 1000.0 / (double) (Constants.FPS);
		if (progressBar != null) {
			gcProgress.setBackground(new Color(Display.getCurrent(), 255, 255,
					255));
			gcProgress.fillRectangle(0, 0, Constants.IMAGE_WIDTH,
					Constants.PROGRESSBAR_HEIGHT);
		}

		while (!dispose) {
			if (state == 0) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (state == 1) {
				if (audios != null) {
					// skip frame
					int nextAudioTime = (int) (interFrameTime * currentVideoFrame);
					long currentTime = duration + System.currentTimeMillis()
							- playTime;

					if ((nextAudioTime < currentTime)) {
						currentVideoFrame++;
						if (currentVideoFrame > (totalVideoFrames - 1)) {
							state = 2;
						} else {
							state = 1;
						}
					} else {
						state = 2;
					}
				}
			} else if (state == 2) {
				if (audios != null) {
					int nextAudioTime = (int) (interFrameTime * currentVideoFrame);
					long currentTime = duration + System.currentTimeMillis()
							- playTime;

					if (nextAudioTime <= currentTime) {
						try {
							if (currentVideoFrame < totalVideoFrames) {
								dataLine.write(audios[currentVideoFrame], 0,
										audios[currentVideoFrame].length);
								currentVideoFrame++;
								state = 1;
							} else {
								dataLine.drain();
								dataLine.close();
								state = 3;
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						try {
							Thread.sleep((int) (nextAudioTime - currentTime));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				}
			}
			// idle
			else if (state == 3) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (asynPlay) {
				state = 2;
				asynPlay = false;
			}
			if (asynLoad) {
				initPlayer();
				asynLoad = false;
			}
			if (asynPause) {
				if (audios != null) {
					dataLine.drain();
				}
				state = 0;
				asynPause = false;
			}
			if (asynStop) {
				if (audios != null) {
					currentVideoFrame = 0;
					initPlayer();
				}

				if (progressBar != null) {
					gcProgress.setBackground(new Color(Display.getCurrent(),
							255, 255, 255));
					gcProgress.fillRectangle(0, 0, Constants.IMAGE_WIDTH,
							Constants.PROGRESSBAR_HEIGHT);
				}

				state = 0;
				asynStop = false;
			}

			if (progressBar != null) {
				gcProgress.setBackground(new Color(Display.getCurrent(), 100,
						100, 255));
				gcProgress.fillRectangle(0, 0, getProgressWidth(),
						Constants.PROGRESSBAR_HEIGHT);
			}
		}
	}

	int getProgressWidth() {
		if (totalVideoFrames == 0) {
			return 0;
		} else {
			return (int) (Constants.IMAGE_WIDTH * (currentVideoFrame) / totalVideoFrames);
		}
	}

	void playAudio(long playTime, long duration) {
		this.playTime = playTime;
		this.duration = duration;
		asynPlay = true;
	}

	void pauseAudio() {
		asynPause = true;
	}

	void stopAudio() {
		asynStop = true;
	}

	void loadAudio(File f, byte[][] audios) {
		this.f = f;
		this.audios = audios;
		asynLoad = true;
	}

	void dispose() {
		dispose = true;
	}
}
