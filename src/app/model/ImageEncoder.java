package app.model;

import app.utilities.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

public class ImageEncoder extends ImageProcessor {
	private String msg;
	private int mask;
	private int shift;
	
	public ImageEncoder (BufferedImage src, String message) {
		super(src);
		this.msg = message;
		this.mask = 0xC0; // 192
		this.shift = 6;
	}
	
	private void embedSecretData (int x, int y, int channel, int pos) {
		int clean = this.pixels[x][y][channel] & 0xFC; // 253
		int secret = (this.msg.charAt(pos) & this.mask) >> this.shift;
		clean += secret;
		this.pixels[x][y][channel] = clean;
		this.mask >>= 2;
		this.shift -= 2;
		if (this.mask == 0) {
		    this.mask = 0xC0; // 192
		    this.shift = 6;
		}
	}
	
	private void setLength (int x, int y, int channel, int shift) {
		int clean = this.pixels[x][y][channel] & 0xFE; // 254
		int secret = (this.msg.length() >> shift) & 1;
		this.pixels[x][y][channel] = clean + ((this.msg.length() == 0x10000) ? 0 : secret); // 65.536
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
	
	private void embedBitLengthIndicator (int x, int y) {
	    this.pixels[x][y][0] &= 0xFD; // 253
	    if (this.msg.length() > 0xFF) 
	        this.pixels[x][y][0] += 0x2;
	}
	
	private void embedBitStartIndication (int start, int layer, boolean clockwise) {
        this.pixels[layer][layer][0] &= 0xFB; // 251
        this.pixels[layer][this.pixels.length - layer - 1][0] &= 0xFB;
        this.pixels[this.pixels.length - layer - 1][this.pixels.length - layer - 1][0] &= 0xFB;
        this.pixels[this.pixels.length - layer - 1][layer][0] &= 0xFB;
        
	    if (start == 0) { // top-left
	        this.pixels[layer][layer][0] += 4;
	        
	        this.pixels[layer][layer][1] &= 0xFB;
	        
	        if (clockwise) {
	            this.pixels[layer][layer][1] += 4;
	        }
        } else if (start == 1) { // top-right
            this.pixels[layer][this.pixels.length - layer - 1][0] += 4;
            
            this.pixels[layer][this.pixels.length - layer - 1][1] &= 0xFB;
            
            if (clockwise) {
                this.pixels[layer][this.pixels.length - layer - 1][1] += 4;
            }
        } else if (start == 2) { // bottom-right
            this.pixels[this.pixels.length - layer - 1][this.pixels.length - layer - 1][0] += 4;
            
            this.pixels[this.pixels.length - layer - 1][this.pixels.length - layer - 1][1] &= 0xFB;
            
            if (clockwise) {
                this.pixels[this.pixels.length - layer - 1][this.pixels.length - layer - 1][1] += 4;
            }
        } else { // bottom-left
            this.pixels[this.pixels.length - layer - 1][layer][0] += 4;
            
            this.pixels[this.pixels.length - layer - 1][layer][1] &= 0xFB;
            
            if (clockwise) {
                this.pixels[this.pixels.length - layer - 1][layer][1] += 4;
            }
        }
	}
	
	public BufferedImage encode () {
	    int bit = (this.msg.length() > 0xFF) ? 16 : 8;
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
		this.embedBitLengthIndicator(0, 0);
		// now, embed the actual message
		int curChar = 0;
		for (; x < this.pixels.length && curChar < this.msg.length(); x++) {
			for (; y < this.pixels[x].length && curChar < this.msg.length(); y++) {
				int id = this.pixels[x][y][indicator] & 0x3; // get last two bits
				switch (id) {
					case 0 : {
						// do nothing
						break; // to prevent unwanted bugs!
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
						if (this.mask == 0xC0) 
						    curChar++;
						
						if (curChar < this.msg.length()) // embed to second channel
							this.embedSecretData(x, y, second, curChar);
						
						break;
					}
				}
				if (this.mask == 0xC0) 
				    curChar++;
			}
		}
		// create the encoded image
		if (curChar < this.msg.length()) {
		    return null;
		} else {
		    return this.createResult();
		}
	}
	
	public BufferedImage encode_v2 () {
	    boolean mark_bit = false;
	    this.initChannel(this.msg.length());
	    int len_idx = 0;
	    int c_idx = 0;
	    
	    int char_count = 0;
	    Random rand = new Random();
	    int bit = (this.msg.length() > 0xFF) ? 16 : 8;
	    int layers = this.pixels.length / 2;
	    
	    for (int i = 0; i <= layers && char_count < this.msg.length(); i++) {
	        int x, y;
	        int start = rand.nextInt(4);
	        int direction = rand.nextInt(2);
	        int idx = start;
	        int increment = direction == 1 ? 1 : -1;
	        
	        if (start == 0) { // top-left
	            x = i;
	            y = i;
	            
	            if (direction != 1)
	                idx = 1;
	        } else if (start == 1) { // top-right
	            x = i;
	            y = this.pixels.length - i - 1;
	            
	            if (direction != 1)
	                idx = 2;
	        } else if (start == 2) { // bottom-right
	            x = this.pixels.length - i - 1;
	            y = this.pixels.length - i - 1;
	            
	            if (direction != 1)
	                idx = 3;
	        } else { // bottom-left
	            x = this.pixels.length - i - 1;
	            y = i;
	            
	            if (direction != 1)
	                idx = 0;
	        }
	        
	        if (!mark_bit) {
	            this.embedBitLengthIndicator(x, y);
	            mark_bit = true;
	        }
	        
	        int start_x = x;
	        int start_y = y;
	        
	        this.embedBitStartIndication(start, i, direction == 1);
	        
	        do {
	            System.out.printf("Bit pos : %d %d\n", x, y);
	            if (bit-- > 0) {
	                this.setLength(x, y, len_idx++, bit);
	                
	                if (len_idx >= 3)
	                    len_idx = 0; 
	            } else {
	                System.out.println("EMBED DATA");
	                int id = this.pixels[x][y][this.indicator_channel[c_idx]] & 0x3;
	                switch (id) {
	                    case 0: {
	                        break;
	                    }
	                    case 1: {
	                        // embed to first channel
	                        this.embedSecretData(x, y, this.first_channel[c_idx], char_count);
	                        break;
	                    }
	                    case 2: {
	                        // embed to second channel
	                        this.embedSecretData(x, y, this.second_channel[c_idx], char_count);
	                        break;
	                    }
	                    default: {
	                        // embed to... both
	                        this.embedSecretData(x, y, this.first_channel[c_idx], char_count);
	                        if (this.mask == 0xC0)
	                            char_count++;
	                        
	                        if (char_count < this.msg.length())
	                            this.embedSecretData(x, y, this.second_channel[c_idx], char_count);
	                        
	                        break;
	                    }
	                }
	                
	                if (this.mask == 0xC0)
	                    char_count++;
	                
	                c_idx++;
	                if (c_idx >= 3)
	                    c_idx = 0;
	                
	                System.out.printf("res : %d %d %d\n", this.pixels[x][y][0], this.pixels[x][y][1], this.pixels[x][y][2]);
	            }

	            x += this.DIRECTION_X[idx];
	            y += this.DIRECTION_Y[idx];
	            
	            if (this.isLimit(x, y, i)) {
	                x -= this.DIRECTION_X[idx];
	                y -= this.DIRECTION_Y[idx];
	                
	                idx += increment;

	                if (idx >= 4)
	                    idx = 0;
	                
	                if (idx <= -1)
	                    idx = 3;
	                
	                x += this.DIRECTION_X[idx];
	                y += this.DIRECTION_Y[idx];
	            }
	        } while (char_count < this.msg.length() && !(x == start_x && y == start_y));
	    }
	    
	    return char_count < this.msg.length() ? null : this.createResult();
	}
}
