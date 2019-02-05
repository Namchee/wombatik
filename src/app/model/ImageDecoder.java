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
	
	public boolean decode (String verification, String algorithm) {
	    int bit = (this.pixels[0][0][0] & 0x2) > 0 ? 16 : 8;
		String res = "";
		int x = 0;
		int y = 0;
		int len = 0;
		// first, let's get the length of secret data
		for (int i = 0; i < bit; i++) {
			len = (len << 1) + (this.pixels[x][y][i % 3] & 1);
			y++;
			if (y >= this.pixels[0].length) {
				y = 0;
				x++;
			}
		}
		if (len == 0) 
		    len = 0x10000;
		
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
						if (this.bit == 8) 
						    res += this.getChar();
						if (res.length() < len) {
							// extract it again from second channel
							this.extractBits(x, y, second);
						}
						break;
					}
				}
				if (this.bit == 8) 
				    res += this.getChar();
			}
		}
		// CALCULATE DIGEST FOR TAMPER DETECTION
		String messageDigest = Utilities.createMessageDigest(res, algorithm);
		return messageDigest.equals(verification);
	}
	
	private void extractBits (int x, int y, int channel) {
		this.code = (this.code << 2) + (this.pixels[x][y][channel] & 0x03);
		this.bit += 2;
	}
	
	private char getChar () {
		char c = (char)this.code;
		this.code = 0;
		this.bit = 0;
		return c;
	}	

	public boolean decode_v2 (String verification, String algorithm) {
	    int layer = this.pixels.length / 2;
	    int cur_layer = 0;
	    String res = "";
	    int len = 0;
	    int x, y, start_x, start_y;
	    int idx, c_idx = 0, increment = 1;
	    
	    int top = this.pixels[cur_layer][cur_layer][0] & 0x04;
        int right = this.pixels[cur_layer][this.pixels.length - cur_layer - 1][0] & 0x04;
        int bottom = this.pixels[this.pixels.length - cur_layer - 1][this.pixels.length - cur_layer - 1][0] & 0x04;
        
        if (top > 0) {
            x = cur_layer;
            y = cur_layer;
            
            idx = 0;
            if ((this.pixels[x][y][1] & 0x04) == 0) {
                idx = 1;
                increment = -1;
            }
        } else if (right > 0) {
            x = cur_layer;
            y = this.pixels.length - cur_layer - 1;
            
            idx = 1;
            if ((this.pixels[x][y][1] & 0x04) == 0) {
                idx = 2;
                increment = -1;
            }
        } else if (bottom > 0) {
            x = this.pixels.length - cur_layer - 1;
            y = this.pixels.length - cur_layer - 1;
            
            idx = 2;
            if ((this.pixels[x][y][1] & 0x04) == 0) {
                idx = 3;
                increment = -1;
            }
        } else {
            x = this.pixels.length - cur_layer - 1;
            y = cur_layer;
            idx = 3;
            
            if ((this.pixels[x][y][1] & 0x04) == 0) {
                idx = 0;
                increment = -1;
            }
        }
        
        start_x = x;
        start_y = y;
        
        int pick_bit = (this.pixels[x][y][0] & 0x02) > 0 ? 16 : 8;
	    
	    for (int i = 0; i < pick_bit; i++) {
	        len = (len << 1) + (this.pixels[x][y][i % 3] & 1);
	        
	        x += this.DIRECTION_X[idx];
	        y += this.DIRECTION_Y[idx];
	        
	        if (this.isLimit(x, y, cur_layer)) {
	            x -= this.DIRECTION_X[idx];
	            y -= this.DIRECTION_Y[idx];
	            
	            idx += increment;
	            if (idx <= -1)
	                idx = 3;
	            if (idx >= 4)
	                idx = 0;
	            
	            x += this.DIRECTION_X[idx];
	            y += this.DIRECTION_Y[idx];
	        }
	        
	        if (x == start_x && y == start_y) { // highly unlikely
	            cur_layer++;
	            top = this.pixels[cur_layer][cur_layer][0] & 0x04;
	            right = this.pixels[cur_layer][this.pixels.length - cur_layer - 1][0] & 0x04;
	            bottom = this.pixels[this.pixels.length - cur_layer - 1][this.pixels.length - cur_layer - 1][0] & 0x04;
	            
	            if (top > 0) {
	                x = cur_layer;
	                y = cur_layer;
	                
	                idx = 0;
	                if ((this.pixels[x][y][1] & 0x04) == 0) {
	                    idx = 1;
	                    increment = -1;
	                }
	            } else if (right > 0) {
	                x = cur_layer;
	                y = this.pixels.length - cur_layer - 1;
	                
	                idx = 1;
	                if ((this.pixels[x][y][1] & 0x04) == 0) {
	                    idx = 2;
	                    increment = -1;
	                }
	            } else if (bottom > 0) {
	                x = this.pixels.length - cur_layer - 1;
	                y = this.pixels.length - cur_layer - 1;
	                
	                idx = 2;
	                if ((this.pixels[x][y][1] & 0x04) == 0) {
	                    idx = 3;
	                    increment = -1;
	                }
	            } else {
	                x = this.pixels.length - cur_layer - 1;
	                y = cur_layer;
	                idx = 3;
	                
	                if ((this.pixels[x][y][1] & 0x04) == 0) {
	                    idx = 0;
	                    increment = -1;
	                }
	            }
	            
	            start_x = x;
	            start_y = y;
	        }
	    }
	    
	    if (len == 0)
	        len = 0x10000;
	    
	    System.out.println(len);
	    
	    this.initChannel(len);
	    
	    while (cur_layer <= layer && res.length() < len) {
	        System.out.printf("Bit pos: %d %d\n", x, y);
	        System.out.println("START");
	        
	        int id = this.pixels[x][y][this.indicator_channel[c_idx]] & 0x3;
	        
	        switch (id) {
	            case 0: {
	                break;
	            }
	            case 1: {
	                System.out.println("One");
	                this.extractBits(x, y, this.first_channel[c_idx]);
	                break;
	            }
	            case 2: {
	                System.out.println("Two");
	                this.extractBits(x, y, this.second_channel[c_idx]);
	                break;
	            }
	            default: {
	                System.out.println("Both");
	                this.extractBits(x, y, this.first_channel[c_idx]);
	                if (this.bit == 8)
	                    res += this.getChar();
	                
	                if (res.length() < len)
	                    this.extractBits(x, y, this.second_channel[c_idx]);
	                
	                break;
	            }
	        }
	        
	        if (this.bit == 8)
	            res += this.getChar();
	        
	        c_idx++;
	        if (c_idx >= 3)
	            c_idx = 0;
	        
	        x += this.DIRECTION_X[idx];
	        y += this.DIRECTION_Y[idx];
	        
	        if (this.isLimit(x, y, cur_layer)) {
                x -= this.DIRECTION_X[idx];
                y -= this.DIRECTION_Y[idx];
                
                idx += increment;
                if (idx <= -1)
                    idx = 3;
                if (idx >= 4)
                    idx = 0;
                
                x += this.DIRECTION_X[idx];
                y += this.DIRECTION_Y[idx];
	        }
	        
	        if (x == start_x && y == start_y) {
                cur_layer++;
                top = this.pixels[cur_layer][cur_layer][0] & 0x04;
                right = this.pixels[cur_layer][this.pixels.length - cur_layer - 1][0] & 0x04;
                bottom = this.pixels[this.pixels.length - cur_layer - 1][this.pixels.length - cur_layer - 1][0] & 0x04;
                
                if (top > 0) {
                    x = cur_layer;
                    y = cur_layer;
                    
                    idx = 0;
                    if ((this.pixels[x][y][1] & 0x04) == 0) {
                        idx = 1;
                        increment = -1;
                    }
                } else if (right > 0) {
                    x = cur_layer;
                    y = this.pixels.length - cur_layer - 1;
                    
                    idx = 1;
                    if ((this.pixels[x][y][1] & 0x04) == 0) {
                        idx = 2;
                        increment = -1;
                    }
                } else if (bottom > 0) {
                    x = this.pixels.length - cur_layer - 1;
                    y = this.pixels.length - cur_layer - 1;
                    
                    idx = 2;
                    if ((this.pixels[x][y][1] & 0x04) == 0) {
                        idx = 3;
                        increment = -1;
                    }
                } else {
                    x = this.pixels.length - cur_layer - 1;
                    y = cur_layer;
                    idx = 3;
                    
                    if ((this.pixels[x][y][1] & 0x04) == 0) {
                        idx = 0;
                        increment = -1;
                    }
                }
                
                start_x = x;
                start_y = y;
            }
	        
	        System.out.println(this.bit);
	    }
	    
	    System.out.println(res);
	    
	    String messageDigest = this.createMessageDigest(res, algorithm);
	    return messageDigest.equals(verification);
	}
}
