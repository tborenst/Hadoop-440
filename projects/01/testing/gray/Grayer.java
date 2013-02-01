package gray;

import java.awt.image.BufferedImage;

import person.Person;
import migratableProcesses.MigratableProcess;
import transactionaFileIO.tFile;

public class Grayer implements MigratableProcess{
	private String path;
	private int pixelX; //col
	private int pixelY; //row
	private String format;
	//private BufferedImage img;
	private tFile file;
	
	private static final long serialVersionUID = 4L;
	private boolean suspended;
	
	public Grayer(String path, String format) {
		this.path = path;
		this.file = new tFile(path);
		//this.img = file.readImg();
		this.format = format;
		this.pixelX = 0;
		this.pixelY = 0;
		
		this.suspended = false;
	}
	
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
	
	private void saveImg(BufferedImage img) {
		System.out.println("Saving Img");
		file.writeImg(img, format);
		System.out.println("Saved Img");
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		BufferedImage img = file.readImg();
		while(!suspended && pixelY < img.getHeight()) {
			if(pixelX == img.getWidth()-1) {pixelX = 0; pixelY++;}
			else {pixelX++;}
			
			grayPixel(img, pixelX, pixelY);
		}
		saveImg(img);
		suspended = false;
		
	}

	@Override
	public void suspend() {
		System.out.println("Suspended!");
		suspended = true;
		while(suspended){System.out.println("asdf");}
	}
	
	public static void main(String[] args) throws InterruptedException {
		Grayer g = new Grayer("testing/gray/test.jpg", "jpeg");
		Thread gt = new Thread(g);
		tFile serFile = new tFile("img.ser");
		
		
		gt.start();
		System.out.println("running...");
		
		Thread.sleep(1000);		
		
		System.out.println("suspending...");
		g.suspend();
		
		System.out.println("check image!!");
		Thread.sleep(100000);
		
		System.out.println("serializing...");
		serFile.writeObj(g);
		
		Thread.sleep(100);
		
		System.out.println("deserializing...");
		Grayer gRevival = (Grayer) serFile.readObj();
		Thread gRevivalT = new Thread(gRevival);
		gRevivalT.start();
	}
}