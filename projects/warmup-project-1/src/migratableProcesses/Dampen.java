/**
 * The Grayer class takes in a String path and String format of an existing image to grayscale the image pixel by pixel.
 */

package migratableProcesses;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;

import processManager.ThreadProcess;
import transactionaFileIO.tFile;
import util.Util;

public class Dampen implements MigratableProcess{
	private int pixelX; //col
	private int pixelY; //row
	private String format;
	private String[] args;
	
	private static final long serialVersionUID = 4L;
	private volatile boolean suspended;
	private tFile file;
	
	public Dampen(String[] args) {
		this(args[0], args[1]);
	}
	
	public Dampen(String path, String format) {
		String[] args = {path, format};
		this.file = new tFile(path, true);
		this.args = args;
		this.format = format;
		this.pixelX = 0;
		this.pixelY = 0;
		
		this.suspended = false;
	}
	
	public String toString() {
		return "Blurer "+Util.stringifyArray(args);
	}
	
	/**
	 * void grayPixel(BufferedImage img, int x, int y):
	 * Average grayscales the a pixel at (x,y) in BufferedImage img .
	 * @param img
	 * @param x
	 * @param y
	 */
	private void dampenPixel(BufferedImage img, int x, int y) {
		if(0 <= x && x < img.getWidth() && 0 <= y && y < img.getHeight()) {
			int rgb = img.getRGB(x,y);
			
			int colorMask = 255;
			int r = (rgb >> 16) & colorMask;
			int newRed = r/5;
			
			int g = (rgb >> 8) & colorMask;
			int newGreen = g/5;
			
			int b = rgb & colorMask;
			int newBlue = b/5;
			
			int newrgb = (newRed<<16)|(newGreen<<8)|(newBlue);
			img.setRGB(x, y, newrgb);
		}
		
	}
	
	/**
	 * void saveImg(BufferedImage img):
	 * Writes the BufferedImage img to file.
	 * @param img
	 */
	private void saveImg(BufferedImage img) {
		file.writeImg(img, format);
	}
	
	/**
	 * void run(void):
	 * Reads the image from file and runs grayPixel one pixel at a time.
	 */
	@Override
	public void run() {
		BufferedImage img = file.readImg();
		while(!suspended && img != null && pixelY < img.getHeight()) {
			//iterate through the image first by column and then by row
			if(pixelX == img.getWidth()-1) {pixelX = 0; pixelY++;}
			else {pixelX++;}
			
			dampenPixel(img, pixelX, pixelY);
		}
		
		//suspended
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
		while(suspended);
	}
	
	//testing
	public static void main(String[] args) throws InterruptedException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		String[] foo = {"_cayman.jpg", "jpeg"};
		ThreadProcess gt = new ThreadProcess("migratableProcesses.Dampen", 98, foo);
		
		gt.start();
		System.out.println("running...");
		
		Thread.sleep(1000);		
		
		System.out.println("suspending...");
		//gt.suspend();("img.ser");
		
		System.out.println("check image!!");
		Thread.sleep(5000);
		
		System.out.println("reviving...");
		//ThreadProcess gtRevival = new ThreadProcess("img.ser", 98, "testing.Blurer", true);
		
		System.out.println("rerunning...");
		//gtRevival.start();
	}

}