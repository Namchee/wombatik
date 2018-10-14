package app.model;

import app.utilities.*;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageEncoder extends ImageProcessor {
	private String msg;
	private int mask;
	
	public ImageEncoder (BufferedImage src, String message) {
		super(src);
		this.msg = message;
		this.mask = 192; // 192 is 1100 0000
	}
	
	private int getShift () {
		for (int i = 0; i < 6; i += 2) {
			if (((this.mask >> i) & 1) == 1) return i;
		}
		return 0x6;
	}
	
	private void editChannel (int x, int y, int channel, int pos) {
		int clean = this.pixels[x][y][channel] & 252;
		int secret = (this.msg.charAt(pos) & this.mask) >> this.getShift();
		clean += secret;
		this.pixels[x][y][channel] = clean;
		this.mask >>= 2;
		if (this.mask == 0) this.mask = 192;
	}
	
	private void setLength (int x, int y, int channel, int shift) {
		int clean = this.pixels[x][y][channel] & 254;
		int secret = (this.msg.length() >> shift) & 1;
		this.pixels[x][y][channel] = clean + secret;
	}
	
	private BufferedImage createResult () {
		BufferedImage res = new BufferedImage(this.pixels.length, this.pixels[0].length, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < this.pixels.length; i++) {
			for (int j = 0; j < this.pixels[i].length; j++) {
				res.setRGB(i, j, new Color(this.pixels[i][j][0], this.pixels[i][j][1], this.pixels[i][j][2]).getRGB());
			}
		}
		return res;
	}
	
	public String calculateDigest () {
	    return Utilities.createMessageDigest(this.msg);
	}
	
	public double calculatePSNR (BufferedImage orig, BufferedImage mod) {
	    return Utilities.calculatePSNR(orig, mod);
	}
	
	public BufferedImage encode () {
		int indicator = Utilities.getIndicatorChannel(this.msg.length());
		int first = Utilities.getFirstChannel(this.msg.length());
		int second = Utilities.getSecondChannel(this.msg.length());
		int x = 0;
		int y = 0;
		// embed secret data length
		// PSST : I will change the algorithm a little bit later
		for (int i = 0; i < 8; i++) {
			this.setLength(x, y, i % 3, 7 - i);
			y++;
			if (y == this.pixels[0].length) {
				x++;
				y = 0;
			}
		}
		// now, embed the actual message
		int curChar = 0;
		for (; x < this.pixels.length && curChar < this.msg.length(); x++) {
			for (; y < this.pixels[x].length && curChar < this.msg.length(); y++) {
				int id = this.pixels[x][y][indicator] & 3; // get last two bits
				switch (id) {
					case 0 : {
						// do nothing
						continue; // to prevent unwanted bugs!
					}
					case 1 : {
						// embed to second channel
						this.editChannel(x, y, second, curChar);
						break;
					}
					case 2 : {
						// embed to first channel
						this.editChannel(x, y, first, curChar);
						break;
					}
					case 3 : {
						// embed to first channel
						this.editChannel(x, y, first, curChar);
						if (this.mask == 192) curChar++;
						if (curChar < this.msg.length()) {
							// and second channel
							this.editChannel(x, y, second, curChar);
						}
						break;
					}
				}
				if (this.mask == 192) curChar++;
			}
		}
		// create the encoded image
		if (curChar < this.msg.length() || this.mask < 192) {
			System.out.println("Image too small for this message! Embedding failed!");
			System.exit(1);
		}
		return this.createResult();
	}
}
