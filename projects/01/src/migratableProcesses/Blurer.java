/**
 * The Grayer class takes in a String path and String format of an existing image to grayscale the image pixel by pixel.
 */

package migratableProcesses;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import processManager.ThreadProcess;
import transactionaFileIO.TransactionalFileInputStream;
import transactionaFileIO.TransactionalFileOutputStream;

public class Blurer implements MigratableProcess{
	private int pixelX; //col
	private int pixelY; //row
	private String format;
	private TransactionalFileOutputStream outStream;
	private TransactionalFileInputStream inStream;
	
	private static final long serialVersionUID = 4L;
	private volatile boolean suspended;
	
	public Blurer(String[] args) {
		this(args[0], args[1]);
	}
	
	public Blurer(String path, String format) {
		this.outStream = new TransactionalFileOutputStream(path);
		this.inStream = new TransactionalFileInputStream(path);
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
	private void blurPixel(BufferedImage img, int x, int y) {
		if(0 <= x && x < img.getWidth() && 0 <= y && y < img.getHeight()) {
			int rgb = img.getRGB(x,y);
			int rgbLeft = rgb;
			int rgbRight = rgb;
			int rgbTop = rgb;
			int rgbBottom = rgb;
			
			if(0 <= x-1) {rgbLeft = img.getRGB(x-1, y);}
			if(x+1 < img.getWidth()) {rgbRight = img.getRGB(x+1, y);}
			if(0 <= y-1) {rgbTop = img.getRGB(x, y-1);}
			if(y+1 < img.getHeight()) {rgbBottom = img.getRGB(x, y+1);}
			
			int colorMask = 255;
			int r = (rgb >> 16) & colorMask;
			int rL = (rgbLeft >> 16) & colorMask;
			int rR = (rgbRight >> 16) & colorMask;
			int rT = (rgbTop >> 16) & colorMask;
			int rB = (rgbBottom >> 16) & colorMask;
			int newRed = (rL+rR+rT+rB+r)/5;
			
			int g = (rgb >> 8) & colorMask;
			int gL = (rgbLeft >> 8) & colorMask;
			int gR = (rgbRight >> 8) & colorMask;
			int gT = (rgbTop >> 8) & colorMask;
			int gB = (rgbBottom >> 8) & colorMask;
			int newGreen = (gL+gR+gT+gB+g)/5;
			
			int b = rgb & colorMask;
			int bL = rgbLeft & colorMask;
			int bR = rgbRight & colorMask;
			int bT = rgbTop & colorMask;
			int bB = rgbBottom & colorMask;
			int newBlue = (bL+bR+bT+bB+b)/5;
			
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
		System.out.println("Trying to Save Img");
		if(outStream != null && img != null ) {
			try {
				ImageIO.write(img, format, outStream);
				System.out.println("Saved Img");
			} catch (IOException e) {
				System.out.println("Unable to Save Img");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * void run(void):
	 * Reads the image from file and runs grayPixel one pixel at a time.
	 */
	@Override
	public void run() {
		if(inStream != null) {
			BufferedImage img = null;
			try {
				img = ImageIO.read(inStream);
			} catch (IOException e) {
				System.out.println("Unable to open original image");
				e.printStackTrace();
			}
			while(!suspended && img != null && pixelY < img.getHeight()) {
				//iterate through the image first by column and then by row
				if(pixelX == img.getWidth()-1) {pixelX = 0; pixelY++;}
				else {pixelX++;}
				
				blurPixel(img, pixelX, pixelY);
			}
			
			//suspended
			saveImg(img);
		}
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
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//testing
	public static void main(String[] args) throws InterruptedException {
		String[] foo = {"testing/test.jpg", "jpeg"};
		ThreadProcess gt = new ThreadProcess("migratableProcesses.Grayer", 98, foo);
		
		gt.start();
		System.out.println("running...");
		
		Thread.sleep(1000);		
		
		System.out.println("suspending...");
		gt.serialize("img.ser");
		
		System.out.println("check image!!");
		Thread.sleep(5000);
		
		System.out.println("reviving...");
		ThreadProcess gtRevival = new ThreadProcess("img.ser", 98, "testing.Grayer", true);
		
		System.out.println("rerunning...");
		gtRevival.start();
	}

}