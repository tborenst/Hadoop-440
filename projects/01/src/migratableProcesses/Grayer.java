/**
 * The Grayer class takes in a String path and String format of an existing image to grayscale the image pixel by pixel.
 */

package migratableProcesses;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;

import processManager.ThreadProcess;
import transactionaFileIO.TransactionalFileInputStream;
import transactionaFileIO.TransactionalFileOutputStream;

public class Grayer implements MigratableProcess{
	private int pixelX; //col
	private int pixelY; //row
	private String format;
	private String[] args;
	private TransactionalFileOutputStream outStream;
	private TransactionalFileInputStream inStream;
	
	private static final long serialVersionUID = 4L;
	private volatile boolean suspended;
	
	public Grayer(String[] args) {
		this(args[0], args[1]);
	}
	
	public Grayer(String path, String format) {
		String[] args = {path, format};
		this.args = args;
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
				
				grayPixel(img, pixelX, pixelY);
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
	public static void main(String[] args) throws InterruptedException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
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