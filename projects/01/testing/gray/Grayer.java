package gray;

import java.awt.image.BufferedImage;
import migratableProcesses.MigratableProcess;
import transactionaFileIO.tFile;

public class Grayer implements MigratableProcess{
	private BufferedImage img;
	private tFile file;
	private int pixelX; //col
	private int pixelY; //row
	private String format;
	
	private boolean suspended;
	
	public Grayer(String path, String format) {
		this.file = new tFile(path);
		this.img = file.readImg();
		this.format = format;
		this.pixelX = 0;
		this.pixelY = 0;
		
		this.suspended = false;
	}
	
	private void grayPixel(int x, int y) {
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
	
	private void saveImg() {
		System.out.println("Saving Img");
		file.writeImg(img, format);
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(!suspended && pixelY < img.getHeight()) {
			if(pixelX == img.getWidth()-1) {pixelX = 0; pixelY++;}
			else {pixelX++;}
			grayPixel(pixelX, pixelY);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		saveImg();
		suspended = false;
		
	}

	@Override
	public void suspend() {
		System.out.println("Suspended!");
		suspended = true;
		while(suspended);		
	}
	
	public static void main(String[] args) {
		Grayer g = new Grayer("testing/gray/test2.jpg", "jpeg");
		Thread gt = new Thread(g);
		gt.start();
		
		System.out.println("running...");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("suspending...");
		g.suspend();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		gt.start();
		
		System.out.println("running...");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("suspending...");
		g.suspend();
		
		gt.start();
		
		System.out.println("finishing...");	
	}
}