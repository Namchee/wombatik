package app.utilities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ImageProcessor {
	protected int[][] pixels;
	
	// V2 only
	protected int[] indicator_channel;
	protected int[] first_channel;
	protected int[] second_channel;
	protected final int[] DIRECTION_X = { 0, 1, 0, -1 };
	protected final int[] DIRECTION_Y = { 1, 0, -1, 0 };
	
	public ImageProcessor (BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		this.pixels = new int[width][height];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color cols = new Color(img.getRGB(x, y));
				this.pixels[x][y] += cols.getRed();
				this.pixels[x][y] = (this.pixels[x][y] << 8) + cols.getGreen();
				this.pixels[x][y] = (this.pixels[x][y] << 8) + cols.getBlue();
				// this.pixels[x][y][0] = cols.getRed();
				// this.pixels[x][y][1] = cols.getGreen();
				// this.pixels[x][y][2] = cols.getBlue();
			}
		}
	}
	
    protected int[] shiftArray (int[] src) {
        int[] res = new int[3];
        int temp = src[2];
        
        for (int i = 2; i > 0; i--) {
            res[i] = src[i - 1];
        }
        
        res[0] = temp;
        
        return res;
    }
    
    protected int[][] swapArray (int[] a, int[] b) {
        int[] a_res = new int[3];
        int[] b_res = new int[3];
        
        for (int i = 0; i < 3; i++) {
            a_res[i] = b[i];
        }
        
        for (int i = 0; i < 3; i++) {
            b_res[i] = a[i];
        }
        
        int[][] res = { a_res, b_res };
        
        return res;
    }
	
	protected int getSetBits (int len) {
        int count = 0;
        while (len > 0) {
            len &= (len - 1);
            count++;
        }
        return count;
    }
    
    protected boolean isPrime (int len) {
        for (int i = 2; i <= len / 2; i++) {
            if (len % i == 0) return false;
        }
        return true;
    }
    
    private byte[] createDigestByte (String str, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] message = str.getBytes(StandardCharsets.UTF_8);
            return digest.digest(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String createMessageDigest (String str, String algorithm) {
        byte[] res = this.createDigestByte(str, algorithm);
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < res.length; i++) {
            if ((0xff & res[i]) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & res[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & res[i]));
            }
        }
        return hexString.toString();
    }
    
    private double calculateMSE (BufferedImage orig, BufferedImage mod) {
        double res = 0.0;
        int width = orig.getWidth();
        int height = orig.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color original = new Color(orig.getRGB(x, y));
                Color modified = new Color(mod.getRGB(x, y));
                res += Math.pow(original.getRed() - modified.getRed(), 2);
                res += Math.pow(original.getGreen() - modified.getGreen(), 2);
                res += Math.pow(original.getBlue() - modified.getBlue(), 2);
            }
        }
        return res / (width * height);
    }
    
    public double calculatePSNR (BufferedImage orig, BufferedImage mod) {
        return 10 * Math.log10(Math.pow(255, 2) / this.calculateMSE(orig, mod));
    }
    
    protected int getRed (int x, int y) {
        return (this.pixels[x][y] & 0xFF0000) >> 16;
    }
    
    protected int getGreen (int x, int y) {
        return (this.pixels[x][y] & 0x00FF00) >> 8;
    }
    
    protected int getBlue (int x, int y) {
        return (this.pixels[x][y] & 0x0000FF);
    }
    
    protected int getColor (int x, int y, int channel) {
        if (channel == 0)
            return this.getRed(x, y);
        else if (channel == 1)
            return this.getGreen(x, y);
        else
            return this.getBlue(x, y);
    }
    
    protected void setRed (int x, int y, int value) {
        this.pixels[x][y] &= 0x00FFFF;
        this.pixels[x][y] += (value << 16);
    }
    
    protected void setGreen (int x, int y, int value) {
        this.pixels[x][y] &= 0xFF00FF;
        this.pixels[x][y] += (value << 8);
    }
    
    protected void setBlue (int x, int y, int value) {
        this.pixels[x][y] &= 0xFFFF00;
        this.pixels[x][y] += value;
    }
    
    protected void setColor (int x, int y, int channel, int value) {
        if (channel == 0)
            this.setRed(x, y, value);
        else if (channel == 1)
            this.setGreen(x, y, value);
        else
            this.setBlue(x, y, value);
    }
    
    // V1
    
    protected int getIndicatorChannel (int len) {
        return (len & 1) == 0 ? 0 : this.isPrime(len) ? 2 : 1;
    }
    
    
    protected int getFirstChannel (int len) {
        int set_bits = this.getSetBits(len) & 1;
        
        switch (this.getIndicatorChannel(len)) {
            case 0 : return set_bits == 0 ? 2 : 1;
            case 1 : return set_bits == 0 ? 2 : 0;
            default : return set_bits == 0 ? 1 : 0;
        }
    }
    
    protected int getSecondChannel (int len) {
        int set_bits = this.getSetBits(len) & 1;
        
        switch (this.getIndicatorChannel(len)) {
            case 0 : return set_bits == 0 ? 1 : 2;
            case 1 : return set_bits == 0 ? 0 : 2;
            default : return set_bits == 0 ? 0 : 1;
        }
    }
    
    // V2
    
    protected void initChannel (int len) {
        boolean prime = this.isPrime(len);
        
        this.indicator_channel = new int[3];
        this.indicator_channel[0] = 0;
        this.indicator_channel[1] = 1;
        this.indicator_channel[2] = 2;
        
        if ((len & 1) == 1) {
            this.indicator_channel = this.shiftArray(this.indicator_channel);
            
            if (!prime)
                this.indicator_channel = this.shiftArray(this.indicator_channel);
        }
        
        this.first_channel = this.shiftArray(this.indicator_channel);
        this.second_channel = this.shiftArray(this.first_channel);
        
        if ((this.getSetBits(len) & 1) == 1) {
            int[][] swap_result = this.swapArray(this.first_channel, this.second_channel);
            
            this.first_channel = swap_result[0];
            this.second_channel = swap_result[1];
        }
    }
    
    protected boolean isLimit (int x, int y, int layer) {
        int lower_limit = layer - 1;
        int upper_limit = this.pixels.length - layer;
        
        return x <= lower_limit || x >= upper_limit || y <= lower_limit || y >= upper_limit;
    }
}
