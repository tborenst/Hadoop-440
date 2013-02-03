/**
 * The Grayer class takes in a String path and String format of an existing image to grayscale the image pixel by pixel.
 */

package gray;

import java.awt.image.BufferedImage;

import networking.SIOCommand;

import person.Person;
import processManager.ThreadProcess;
import migratableProcesses.MigratableProcess;
import transactionaFileIO.tFile;

public class Grayer implements MigratableProcess{
	private String path;
	private int pixelX; //col
	private int pixelY; //row
	private String format;
	private tFile file;
	
	private static final long serialVersionUID = 4L;
	private boolean suspended;
	
	public Grayer(String path, String format) {
		this.path = path;
		this.file = new tFile(path);
		this.format = format;
		this.pixelX = 0;
		this.pixelY = 0;
		
		this.suspended = false;
	}
	
	/**
	 * void grayPixel(BufferedImage img, int x, int y):
	 * Average grayscales the a pixel at (x,y) in BufferedImage img .
	 * @param img
	 * @param x
	 * @param y
	 */
	private void grayPixel(BufferedImage img, int x, int y) {
		if(0 <= x && x < img.getWidth() && 0 <= y && y < img.getHeight()) {
			int rgb = img.getRGB(x,y);
			int colorMask = 255;
			int r = (rgb >> 16) & colorMask;
			int g = (rgb >> 8) & colorMask;
			int b = rgb & colorMask;
			int newColor = (r+g+b)/3;
			int newrgb = (newColor<<16)|(newColor<<8)|(newColor);
			img.setRGB(x, y, newrgb);
		}
		
	}
	
	/**
	 * void saveImg(BufferedImage img):
	 * Writes the BufferedImage img to file.
	 * @param img
	 */
	private void saveImg(BufferedImage img) {
		System.out.println("Saving Img");
		file.writeImg(img, format);
		System.out.println("Saved Img");
	}
	
	/**
	 * void run(void):
	 * Reads the image from file and runs grayPixel one pixel at a time.
	 */
	@Override
	public void run() {		
		BufferedImage img = file.readImg();
		while(!suspended && pixelY < img.getHeight()) {
			//iterate through the image first by column and then by row
			if(pixelX == img.getWidth()-1) {pixelX = 0; pixelY++;}
			else {pixelX++;}
			
			grayPixel(img, pixelX, pixelY);
		}
		
		saveImg(img);
		suspended = false;
		
	}

	/**
	 * void suspend(void):
	 * Suspends run().
	 */
	@Override
	public void suspend() {
		System.out.println("Suspended!");
		suspended = true;
		while(suspended){
			//System.out.println("asdf");
		}
	}
	
	//testing
	public static void main(String[] args) throws InterruptedException {
		
		
		
		//TODO: include id
		ThreadProcess gt = new ThreadProcess((MigratableProcess) new Grayer("testing/gray/test.jpg", "jpeg"));
		
		gt.start();
		System.out.println("running...");
		
		Thread.sleep(1000);		
		
		System.out.println("suspending...");
		gt.serialize("img.ser");
		
		System.out.println("check image!!");
		Thread.sleep(10000);
		
		System.out.println("deserializing...");
		tFile serFile = new tFile("img.ser");
		ThreadProcess gtRevival = new ThreadProcess((MigratableProcess) serFile.readObj());
		gtRevival.start();
	}

}