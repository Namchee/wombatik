package app.model;

import app.utilities.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

public class ImageEncoder extends ImageProcessor {
    private String msg;
    private int mask;
    private int shift;

    public ImageEncoder(BufferedImage src, String message) {
        super(src);
        this.msg = message;
        this.mask = 0xC0; // 192
        this.shift = 6;
    }

    private void embedSecretData(int x, int y, int channel, int pos) {
        int clean = this.getColor(x, y, channel) & 0xFC; // 253
        int secret = (this.msg.charAt(pos) & this.mask) >> this.shift;
        clean += secret;
        this.setColor(x, y, channel, clean);
        this.mask >>= 2;
        this.shift -= 2;
        if (this.mask == 0) {
            this.mask = 0xC0; // 192
            this.shift = 6;
        }
    }

    private void setLength(int x, int y, int channel, int shift) {
        // System.out.println(x + " " + y);
        int clean = this.getColor(x, y, channel) & 0xFE; // 254
        int secret = (this.msg.length() >> shift) & 1;
        clean += ((this.msg.length() == 0x10000) ? 0 : secret); // 65.536
        this.setColor(x, y, channel, clean);
    }

    private BufferedImage createResult() {
        BufferedImage res = new BufferedImage(this.pixels.length, this.pixels[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < this.pixels.length; i++) {
            for (int j = 0; j < this.pixels[i].length; j++) {
                res.setRGB(i, j, new Color(this.getRed(i, j), this.getGreen(i, j), this.getBlue(i, j)).getRGB());
            }
        }
        return res;
    }

    private void embedBitLengthIndicator (int x, int y) {
        if (this.msg.length() > 0xFF)
            this.setRed(x, y, (this.getRed(x, y) & 0xFD) + 2);
        else
            this.setRed(x, y, (this.getRed(x, y) & 0xFD));
    }

    private void embedBitStartIndication (int start, int layer, boolean clockwise) {  
        int x, y;
        if (start == 0) { // top-left
            x = layer;
            y = layer;
        } else if (start == 1) { // top-right
            x = layer;
            y = this.pixels.length - layer - 1;
        } else if (start == 2) { // bottom-right
            x = this.pixels.length - layer - 1;
            y = this.pixels.length - layer - 1;
        } else { // bottom-left
            x = this.pixels.length - layer - 1;
            y = layer;
        }
        
        this.setRed(layer, layer, this.getRed(layer, layer) & 0xFB); // 251
        this.setRed(layer, this.pixels.length - layer - 1, this.getRed(layer, this.pixels.length - layer - 1) & 0xFB);
        this.setRed(this.pixels.length - layer - 1, this.pixels.length - layer - 1, this.getRed(this.pixels.length - layer - 1, this.pixels.length - layer - 1) & 0xFB);
        this.setRed(this.pixels.length - layer - 1, layer, this.getRed(this.pixels.length - layer - 1, layer) & 0xFB);
        
        this.setGreen(layer, layer, this.getGreen(layer, layer) & 0xFB); // 251
        this.setGreen(layer, this.pixels.length - layer - 1, this.getGreen(layer, this.pixels.length - layer - 1) & 0xFB);
        this.setGreen(this.pixels.length - layer - 1, this.pixels.length - layer - 1, this.getGreen(this.pixels.length - layer - 1, this.pixels.length - layer - 1) & 0xFB);
        this.setGreen(this.pixels.length - layer - 1, layer, this.getGreen(this.pixels.length - layer - 1, layer) & 0xFB);

        this.setRed(x, y, this.getRed(x, y) + 4);
        
        if (clockwise) 
            this.setGreen(x, y, this.getGreen(x, y) + 4);
    }

    public BufferedImage encode() {
        int bit = (this.msg.length() > 0xFF) ? 16 : 8;
        int indicator = this.getIndicatorChannel(this.msg.length());
        int first = this.getFirstChannel(this.msg.length());
        int second = this.getSecondChannel(this.msg.length());
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
                int id = this.getColor(x, y, indicator) & 0x03; // get last two bits
                
                if (id > 0) {
                    switch (id) {
                        case 1: {
                            // embed to second channel
                            this.embedSecretData(x, y, second, curChar);
                            break;
                        }
                        case 2: {
                            // embed to first channel
                            this.embedSecretData(x, y, first, curChar);
                            break;
                        }
                        case 3: {
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
        }
        // create the encoded image
        if (curChar < this.msg.length()) {
            return null;
        } else {
            return this.createResult();
        }
    }
    
    // V2 methods
    
    private int[] getStartProperties (int layer, int corner, boolean clockwise) {
        int[] res = new int[3];
        res[2] = corner;
        
        if (corner == 0) {
            res[0] = layer;
            res[1] = layer;
        } else if (corner == 1) {
            res[0] = layer;
            res[1] = this.pixels.length - layer - 1;
        } else if (corner == 2) {
            res[0] = this.pixels.length - layer - 1;
            res[1] = this.pixels.length - layer - 1;
        } else {
            res[0] = this.pixels.length - layer - 1;
            res[1] = layer;
        }
        
        if (!clockwise)
            res[2]++;
        
        if (res[2] >= 4)
            res[2] = 0;
        
        return res;
    }
    
    private int embedData (int x, int y, int c_idx, int char_pos) {
        int embedded = 0;
        int id = this.getColor(x, y, this.indicator_channel[c_idx]) & 0x03;
        
        if (id > 0) {
            switch (id) {
            case 1:
                this.embedSecretData(x, y, this.first_channel[c_idx], char_pos);
                break;
            case 2:
                this.embedSecretData(x, y, this.second_channel[c_idx], char_pos);
                break;
            case 3:
                this.embedSecretData(x, y, this.first_channel[c_idx], char_pos);
                if (this.mask == 0xC0) {
                    embedded++;
                    char_pos++;
                }         

                if (char_pos < this.msg.length())
                    this.embedSecretData(x, y, this.second_channel[c_idx], char_pos);

                break;
            }

            if (this.mask == 0xC0) {
                embedded++;
                char_pos++;
            }
        }

        return embedded;
    }

    public BufferedImage encode_v2() {
        boolean mark_bit = false;
        this.initChannel(this.msg.length());
        int len_idx = 0;
        int c_idx = 0;

        int char_count = 0;
        Random rand = new Random();
        int bit = (this.msg.length() > 0xFF) ? 16 : 8;
        int layers = this.pixels.length / 2;

        for (int i = 0; i <= layers && char_count < this.msg.length(); i++) {
            int start = rand.nextInt(4);
            int direction = rand.nextInt(2);
            int increment = (direction == 1) ? 1 : -1;

            int[] props = this.getStartProperties(i, start, direction == 1);
            int x = props[0];
            int y = props[1];
            int idx = props[2];

            // embed how many bits to read for length when decoding
            if (!mark_bit) {
                // System.out.println(Integer.toBinaryString(this.getRed(x, y)));
                this.embedBitLengthIndicator(x, y);
                // System.out.println(Integer.toBinaryString(this.getRed(x, y)));
                mark_bit = true;
            }

            int start_x = x;
            int start_y = y;

            this.embedBitStartIndication(start, i, direction == 1);

            do {
                if (bit-- > 0) {
                    this.setLength(x, y, len_idx++, bit);

                    if (len_idx >= 3)
                        len_idx = 0;
                } else {
                    int embed = this.embedData(x, y, c_idx, char_count);
                    char_count += embed;

                    c_idx++;
                    if (c_idx >= 3)
                        c_idx = 0;
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
