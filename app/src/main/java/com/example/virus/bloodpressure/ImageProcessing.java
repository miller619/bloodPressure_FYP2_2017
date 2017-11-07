package com.example.virus.bloodpressure;

/**
 * Created by virus on 11/6/2017.
 */

public class ImageProcessing {
    private static int YUV420SPtoYSum(byte[] yuv420sp, int width, int height, int type) {

        if (yuv420sp == null)
            return 0;

        final int frameSize = width * height;


        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int sum=0;
        int ySum = 0;
        int rSum = 0;
        int gSum = 0;
        int bSum = 0;

        for (int i = 0, ci = ii; i < height ; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv420sp[ci * width + cj]));
                int v = (0xff & ((int) yuv420sp[frameSize + (ci >> 1) * width + (cj & ~1)]));
                int u = (0xff & ((int) yuv420sp[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                int pixel = 0xff000000 | (r << 16) | (g << 8) | b;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel&0xff;
                rSum += red;
                gSum +=green;
                bSum +=blue;
                ySum += y;
            }
        }
        switch(type){
            case (1): sum =rSum;
                break;
            case (2): sum =gSum;
                break;
            case (3): sum =bSum;
                break;
            case (4): sum = ySum;
        }
        return sum;
    }

    public static double decodeYUV420SPtoRedBlueGreenAvg(byte[] yuv420sp, int width, int height, int type) {
        if (yuv420sp == null) return 0;
        final int frameSize = width * height;

        int sum = YUV420SPtoYSum(yuv420sp, width, height, type);
        int mean = (sum / frameSize);

        return mean;
    }
}
