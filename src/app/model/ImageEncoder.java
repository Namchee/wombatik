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
		this.mask = 0xC0; // 192
	}
	
	private int getShift () {
		for (int i = 0; i < 6; i += 2) {
			if (((this.mask >> i) & 0x1) == 1) return i;
		}
		return 0x6;
	}
	
	private void embedSecretData (int x, int y, int channel, int pos) {
		int clean = this.pixels[x][y][channel] & 0xFC; // 253
		int secret = (this.msg.charAt(pos) & this.mask) >> this.getShift();
		clean += secret;
		this.pixels[x][y][channel] = clean;
		this.mask >>= 0x2;
		if (this.mask == 0) this.mask = 0xC0; // 192
	}
	
	private void setLength (int x, int y, int channel, int shift) {
		int clean = this.pixels[x][y][channel] & 0xFE; // 254
		int secret = (this.msg.length() >> shift) & 0x1;
		this.pixels[x][y][channel] = clean + ((this.msg.length() == 0x10000) ? 0 : secret); // 65536
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
	
	public String calculateDigest (String algorithm) {
	    return Utilities.createMessageDigest(this.msg, algorithm);
	}
	
	public double calculatePSNR (BufferedImage orig, BufferedImage mod) {
	    return Utilities.calculatePSNR(orig, mod);
	}
	
	public void embedBitIndicator () {
	    this.pixels[0][0][0] &= 0xFD; // 253
	    if (this.msg.length() > 0xFF) this.pixels[0][0][0] += 0x2;
	}
	
	public BufferedImage encode () {
	    int bit = (this.msg.length() > 0xFF) ? 0x10 : 0x8;
		int indicator = Utilities.getIndicatorChannel(this.msg.length());
		int first = Utilities.getFirstChannel(this.msg.length());
		int second = Utilities.getSecondChannel(this.msg.length());
		int x = 0;
		int y = 0;
		// embed secret data length
		for (int i = 0; i < bit; i++) {
			this.setLength(x, y, i % 3, (bit - 1) - i);
			y++;
			if (y >= this.pixels[0].length) {
				x++;
				y = 0;
			}
		}
		// put a mark to show how many bits should be taken in decoding process
		this.embedBitIndicator();
		// now, embed the actual message
		int curChar = 0;
		for (; x < this.pixels.length && curChar < this.msg.length(); x++) {
			for (; y < this.pixels[x].length && curChar < this.msg.length(); y++) {
				int id = this.pixels[x][y][indicator] & 0x3; // get last two bits
				switch (id) {
					case 0 : {
						// do nothing
						continue; // to prevent unwanted bugs!
					}
					case 1 : {
						// embed to second channel
						this.embedSecretData(x, y, second, curChar);
						break;
					}
					case 2 : {
						// embed to first channel
						this.embedSecretData(x, y, first, curChar);
						break;
					}
					case 3 : {
						// embed to first channel
						this.embedSecretData(x, y, first, curChar);
						if (this.mask == 0xC0) curChar++;
						if (curChar < this.msg.length()) {
							// and second channel
							this.embedSecretData(x, y, second, curChar);
						}
						break;
					}
				}
				if (this.mask == 0xC0) curChar++;
			}
		}
		// create the encoded image
		if (curChar < this.msg.length()) {
		    return null;
		} else {
		    return this.createResult();
		}
	}
}
