package edu.usc.cs576;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class VideoPlayer extends Thread {

	// thread control
	boolean dispose;

	// graphics
	Composite parent;
	Composite progressBar;
	ImageData imageData;
	GC gc;
	GC gcProgress;

	int frameCount;
	int frameN;
	byte[][] frames;
	Image[] images;

	// player control
	int state;
	boolean asynPlay;
	boolean asynPause;
	boolean asynStop;
	boolean asynLoad;
	double progress;
	int percentage;
	double fileSize;

	// sync control
	long playTime;
	long duration;

	VideoPlayer(Composite parent, Composite progressBar) {
		// init thread control
		dispose = false;

		// init graphical
		imageData = new ImageData(Constants.IMAGE_WIDTH,
				Constants.IMAGE_HEIGHT, 24, new PaletteData(0xFF0000, 0xFF00,
						0xFF));

		gc = new GC(parent);
		gcProgress = new GC(progressBar);

		// init player control
		state = 0;
		asynPlay = false;
		asynPause = false;
		asynStop = false;
		asynLoad = false;
	}

	void loadImages() {
		frameN = frames.length;
		frameCount = 0;
		images = new Image[frameN];

		for (int i = 0; i < frameN; i++) {
			imageData.data = frames[i];
			images[i] = new Image(Display.getCurrent(), imageData);
		}

		// reset
		// draw progress
		gcProgress
				.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		gcProgress.fillRectangle(0, 0, Constants.IMAGE_WIDTH,
				Constants.PROGRESSBAR_HEIGHT);

		// init draw
		if (images != null)
			gc.drawImage(images[0], 0, 0);
	}

	@Override
	public void run() {
		// sync control
		double interFrameTime = 1000.0 / (double) (Constants.FPS);
		int state = 0;

		// draw progress
		gcProgress
				.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		gcProgress.fillRectangle(0, 0, Constants.IMAGE_WIDTH,
				Constants.PROGRESSBAR_HEIGHT);

		while (!dispose) {
			// pause
			if (state == 0) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// play frame skipping
			else if (state == 1) {
				if (images != null) {
					int nextImageTime = (int) (interFrameTime * frameCount);
					long currentTime = duration + System.currentTimeMillis()
							- playTime;

					if ((nextImageTime < currentTime)) {
						frameCount++;
						if (frameCount >= (frameN - 1)) {
							state = 2;
						} else {
							state = 1;
						}
					} else {
						state = 2;
					}
				}
			}
			// wait to be played
			else if (state == 2) {
				if (images != null) {
					int nextImageTime = (int) (interFrameTime * frameCount);
					long currentTime = duration + System.currentTimeMillis()
							- playTime;

					if (nextImageTime <= currentTime) {
						if (frameCount >= (frameN - 1)) {
							if (images != null) {
								frameCount = frameN - 1;
								gc.drawImage(images[frameCount], 0, 0);
							}
							state = 3;
						} else {
							gc.drawImage(images[frameCount], 0, 0);
							frameCount++;
							state = 1;
						}
					} else {
						try {
							Thread.sleep(nextImageTime - currentTime);
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
				state = 1;
				asynPlay = false;
			}
			if (asynPause) {
				state = 0;
				asynPause = false;
			}
			if (asynStop) {
				if (images != null) {
					frameCount = 0;
					gc.drawImage(images[0], 0, 0);
					frameCount = 1;
					gcProgress.setBackground(new Color(Display.getCurrent(),
							255, 255, 255));
					gcProgress.fillRectangle(0, 0, Constants.IMAGE_WIDTH,
							Constants.PROGRESSBAR_HEIGHT);
					state = 0;
					asynStop = false;
				}
			}
			if (asynLoad) {
				loadImages();
				state = 0;
				asynLoad = false;
			}

			gcProgress.setBackground(new Color(Display.getCurrent(), 100, 100,
					255));
			gcProgress.fillRectangle(0, 0, getProgressWidth(),
					Constants.PROGRESSBAR_HEIGHT);
		}
	}

	int getProgressWidth() {
		if (frameN == 0) {
			return 0;
		} else {
			return (int) (Constants.IMAGE_WIDTH * (frameCount + 1) / frameN);
		}
	}

	void playVideo(long playTime, long duration) {
		this.playTime = playTime;
		this.duration = duration;
		asynPlay = true;
	}

	void pauseVideo() {
		asynPause = true;
	}

	void stopVideo() {
		asynStop = true;
	}

	void loadVideo(byte[][] frames) {
		this.frames = frames;
		asynLoad = true;
	}

	void dispose() {
		dispose = true;
	}
}
