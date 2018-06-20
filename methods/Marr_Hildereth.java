package methods;

import controller.EdgeDetectionController;

import java.awt.image.BufferedImage;

public class Marr_Hildereth extends EdgeMath{
    private BufferedImage output_img, blurred_img, raw_image;
    private int width, height, kernel;
    private double[][] conv1, magn;
    EdgeDetectionController edc;

    public Marr_Hildereth(BufferedImage img, int kernel, double sigma, EdgeDetectionController edc) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.raw_image = img;
        this.kernel = kernel;
        this.edc = edc;


        output_img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        blurred_img =  new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        conv1 = gaussianKernel(sigma, kernel);
        //DULEZITA cast marr_hildereta
        logBlur();
        conv1 = new double[][]{
                {-1, -1, -1},
                {-1, 8, -1},
                {-1, -1, -1}
        };
        laplaceFilter();
        zeroCrossing();
    }

    private void zeroCrossing() {
        int magnitude;
        for (int y = 2; y < height - 2; y++) {
            for (int x = 2; x < width - 2; x++) {
                if (magn[x - 1][y - 1] * magn[x + 1][y + 1] < 0) {
                    magnitude = 255;
                    int gs = (magnitude << 16) + (magnitude << 8) + magnitude;
                    output_img.setRGB(x, y, gs);

                } else if (magn[x - 1][y] * magn[x + 1][y] < 0) {
                    magnitude = 255;
                    int gs = (magnitude << 16) + (magnitude << 8) + magnitude;
                    output_img.setRGB(x, y, gs);

                } else if (magn[x][y - 1] * magn[x][y + 1] < 0) {
                    magnitude = 255;
                    int gs = (magnitude << 16) + (magnitude << 8) + magnitude;
                    output_img.setRGB(x, y, gs);

                } else if (magn[x + 1][y - 1] * magn[x - 1][y + 1] < 0) {
                    magnitude = 255;
                    int gs = (magnitude << 16) + (magnitude << 8) + magnitude;
                    output_img.setRGB(x, y, gs);

                } else {
                    magnitude = 0;
                    int gs = (magnitude << 16) + (magnitude << 8) + magnitude;
                    output_img.setRGB(x, y, gs);
                }
            }
            double progress = y/(double)height;
            edc.updProg("Zero crossing ", progress);
        }
    }

    private void logBlur() {
        int dyk = kernel/2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // ziskej okoli
                int[][] gray = new int[kernel][kernel];
                for (int j = 0; j < kernel; j++) {
                    for (int i = 0; i < kernel; i++) {
                        if (x<dyk || y <dyk || x> width-dyk-1|| y>height-dyk-1) gray[i][j] = raw_image.getRGB(x, y) & 0xFF;
                        else gray[i][j] = raw_image.getRGB(x - dyk + i, y - dyk + j) & 0xFF;
                    }
                }

                // aplikuj filtr
                int blurred = 0;
                for (int j = 0; j < kernel; j++) {
                    for (int i = 0; i < kernel; i++) {
                        blurred += gray[i][j] * conv1[i][j];
                    }
                }

                int magnitude = (int)(blurred*(1/getKernelSum(conv1)));
//
//                String str = String.format("%10s", magnitude+"");
//                System.out.print(str);

                int gs = (magnitude<<16)+(magnitude<<8)+magnitude;
                blurred_img.setRGB(x, y, gs);
            }
            double progress = y / (double) height;
            edc.updProg("Blurring ", progress);
//            System.out.println();
        }
    }

    private void laplaceFilter(){
        magn = new double[width-1][height-1];

        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                // ziskej okoli
                int[][] gray = new int[3][3];
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 3; i++) {
                        gray[i][j] = blurred_img.getRGB(x-1+i,y-1+j) & 0xFF;
                    }
                }

                // aplikuj filtr
                int gray1 = 0, gray2 = 0;
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 3; i++) {
                        gray1 += gray[i][j] * conv1[i][j];
                    }
                }

                magn[x][y] = Math.round(gray1);
            }
            double progress = y/(double)height;
            edc.updProg("Applying Laplace ", progress);
        }
    }

    public BufferedImage getOutput_img() {
        return output_img;
    }
}
