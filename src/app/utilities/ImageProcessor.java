package app.utilities;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor {
	protected int[][][] pixels;
	
	public ImageProcessor (BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		this.pixels = new int[width][height][3];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color cols = new Color(img.getRGB(x, y));
				this.pixels[x][y][0] = cols.getRed();
				this.pixels[x][y][1] = cols.getGreen();
				this.pixels[x][y][2] = cols.getBlue();
			}
		}
	}
}
