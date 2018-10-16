package app.model;

import app.utilities.*;
import java.awt.image.BufferedImage;

public class ImageDecoder extends ImageProcessor {
	private int code;
	private int bit;
	
	public ImageDecoder (BufferedImage src) {
		super(src);
		this.code = 0;
		this.bit = 0;
	}
	
	public String decode (String verification, String algorithm) {
		String res = "";
		int x = 0;
		int y = 0;
		int len = 0;
		// first, let's get the length of secret data
		for (int i = 0; i < 8; i++) {
			len = (len << 1) + (this.pixels[x][y][i % 3] & 1);
			y++;
			if (y >= this.pixels[0].length) {
				y = 0;
				x++;
			}
		}
		int indicator = Utilities.getIndicatorChannel(len);
		int first = Utilities.getFirstChannel(len);
		int second = Utilities.getSecondChannel(len);
		// now, retrieve the actual message
		for (; x < this.pixels.length && res.length() < len; x++) {
			for (; y < this.pixels[x].length && res.length() < len; y++) {
				int id = this.pixels[x][y][indicator] & 3;
				switch (id) {
					case 0 : {
						// nothing hidden
						break;
					}
					case 1 : {
						// there's a secret data hidden in second channel
						this.extractBits(x, y, second);
						break;
					}
					case 2 : {
						// there's a secret data hidden in first channel
						this.extractBits(x, y, first);
						break;
					}
					case 3 : {
						// there're secret data(s) hidden in both first and second channel
						this.extractBits(x, y, first);
						if (this.bit == 8) res += this.getChar();
						if (res.length() < len) {
							// extract it again from second channel
							this.extractBits(x, y, second);
						}
						break;
					}
				}
				if (this.bit == 8) res += this.getChar();
			}
		}
		// CALCULATE DIGEST FOR TAMPER DETECTION
		String messageDigest = Utilities.createMessageDigest(res, algorithm);
		if (!messageDigest.equals(verification)) {
		    return null;
		}
		return res;
	}
	
	private void extractBits (int x, int y, int channel) {
		this.code = (this.code << 2) + (this.pixels[x][y][channel] & 3);
		this.bit += 2;
	}
	
	private char getChar () {
		char c = (char)this.code;
		this.code = 0;
		this.bit = 0;
		return c;
	}
}
