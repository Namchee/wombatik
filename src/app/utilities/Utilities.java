package app.utilities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Utilities {
	private static int getSetBits (int len) {
		int count = 0;
		while (len > 0) {
			len &= (len - 1);
			count++;
		}
		return count;
	}
	
	private static boolean isPrime (int len) {
		for (int i = 2; i <= len / 2; i++) {
			if (len % i == 0) return false;
		}
		return true;
	}
	
	public static int getIndicatorChannel (int len) {
		return len % 2 == 0 ? 0 : isPrime(len) ? 2 : 1;
	}
	
	
	public static int getFirstChannel (int len) {
		switch (getIndicatorChannel(len)) {
			case 0 : return getSetBits(len) % 2 == 0 ? 2 : 1;
			case 1 : return getSetBits(len) % 2 == 0 ? 2 : 0;
			default : return getSetBits(len) % 2 == 0 ? 1 : 0;
		}
	}
	
	public static int getSecondChannel (int len) {
		switch (getIndicatorChannel(len)) {
			case 0 : return getSetBits(len) % 2 == 0 ? 1 : 2;
			case 1 : return getSetBits(len) % 2 == 0 ? 0 : 2;
			default : return getSetBits(len) % 2 == 0 ? 0 : 1;
		}
	}
	
	public static byte[] createDigestByte (String str) {
	    try {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] message = str.getBytes(StandardCharsets.UTF_8);
	        return digest.digest(message);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public static String createMessageDigest (String str) {
	    byte[] res = createDigestByte(str);
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
	
	public static double calculateMSE (BufferedImage orig, BufferedImage mod) {
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
	
	public static double calculatePSNR (BufferedImage orig, BufferedImage mod) {
	    return 10 * Math.log10(Math.pow(255, 2) / calculateMSE(orig, mod));
	}
}
