package methods;

import controller.EdgeDetectionController;

import java.awt.image.BufferedImage;

public class Canny extends EdgeMath {
    private BufferedImage output_img, blurred_img, rawImage;
    private double[][] conv1, conv2, grads;
    private int width, height, kernel;
    private EdgeDetectionController edc;

    private final int mainTRESHOLD = 20;

    public Canny(BufferedImage img, int kernel, double sigma, double lowerT, double upperT, EdgeDetectionController edc) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.kernel = kernel;
        this.rawImage = img;
        this.edc = edc;

        output_img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        blurred_img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        conv1 = gaussianKernel(sigma, kernel);
//        conv1  = getGausMatrix(kernel);

        //DULEZITA cast cannyho
        doGaussian();
        setSobelConv();
        findEdges();
        nonMaximumSuppression();
        doubleTreshold(lowerT, upperT);
    }

    private void doubleTreshold(double lowerT, double upperT) {
        int magnitude;
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                magnitude = output_img.getRGB(x, y) & 0xFF;
                if (magnitude<Math.round(lowerT*255)) {
                    magnitude = 0;
                    int gs = (magnitude << 16) + (magnitude << 8) + magnitude;
                    output_img.setRGB(x, y, gs);
                }else if (magnitude>Math.round(upperT*255)){
                   //this is fine, im enjoying a tea.. in a beautiful house.. which is on fire.. my dog is on fire.. my tea is on fire.. i'm on fire... actually everything is on fire.......
                }else{
                    if( ((output_img.getRGB(x - 1, y - 1) & 0xFF)<upperT) &&
                        ((output_img.getRGB(x - 1, y + 1) & 0xFF)<upperT) &&
                        ((output_img.getRGB(x + 1, y - 1) & 0xFF)<upperT) &&
                        ((output_img.getRGB(x + 1, y + 1) & 0xFF)<upperT) &&
                        ((output_img.getRGB(x + 1, y) & 0xFF)<upperT) &&
                        ((output_img.getRGB(x - 1, y) & 0xFF)<upperT) &&
                        ((output_img.getRGB(x, y + 1) & 0xFF)<upperT) &&
                        ((output_img.getRGB(x, y - 1) & 0xFF)<upperT))
                    {
                        magnitude = 0;
                        int gs = (magnitude << 16) + (magnitude << 8) + magnitude;
                        output_img.setRGB(x, y, gs);
                    }
                }
            }
            double progress = y / (double) height;
            edc.updProg("double tresholding ", progress);
        }

    }

    private void findEdges() {
        grads = new double[width][height];
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // ziskej okoli
                int[][] gray = new int[3][3];
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 3; i++) {
                        gray[i][j] = blurred_img.getRGB(x - 1 + i, y - 1 + j) & 0xFF;
                    }
                }

                // aplikuj filtr
                int gray1 = 0, gray2 = 0;
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 3; i++) {
                        gray1 += gray[i][j] * conv1[i][j];
                        gray2 += gray[i][j] * conv2[i][j];
                    }
                }

                double gradient = Math.atan(gray1);
                int magnitude = truncate((int) (Math.sqrt(gray1*gray1+gray2*gray2)));
                if (gray1!=0) gradient = Math.atan2(gray2,gray1);
                magnitude = thresholding(magnitude);
                grads[x][y] = Math.round((gradient*180/Math.PI)/45)*45;

//                String str = String.format("%10s", grads[x][y]+"");
//                System.out.print(str);

                int gs = (magnitude<<16)+(magnitude<<8)+magnitude;
                rawImage.setRGB(x, y, gs);
            }
//            System.out.println();
            double progress = y / (double) height;
            edc.updProg("Sobel ", progress);
        }
    }

    private void nonMaximumSuppression() {
        int magnitude;
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                magnitude = rawImage.getRGB(x,y) & 0xFF;
                if (grads[x][y] == 0 || grads[x][y] == 180 || grads[x][y] == -180){
                    if (magnitude < (rawImage.getRGB(x,y-1) & 0xFF) || magnitude < (rawImage.getRGB(x,y+1) & 0xFF)) magnitude = 0;
                }else if (grads[x][y] == 90 || grads[x][y] == 270 || grads[x][y] == -90 || grads[x][y] == -270) {
                    if (magnitude < (rawImage.getRGB(x+1, y) & 0xFF) || magnitude < (rawImage.getRGB(x-1, y) & 0xFF)) magnitude = 0;
                }else if (grads[x][y] == 45 || grads[x][y] == 225 || grads[x][y] == -45 || grads[x][y] == -225){
                    if (magnitude < (rawImage.getRGB(x-1,y-1) & 0xFF) || magnitude < (rawImage.getRGB(x+1,y+1) & 0xFF)) magnitude = 0;
                }else if (grads[x][y] == 135 || grads[x][y] == 315 || grads[x][y] == -135 || grads[x][y] == -315) {
                    if (magnitude < (rawImage.getRGB(x-1,y+1) & 0xFF) || magnitude < (rawImage.getRGB(x+1,y-1) & 0xFF)) magnitude = 0;
                }
                int gs = (magnitude<<16)+(magnitude<<8)+magnitude;
                output_img.setRGB(x, y, gs);
            }
            double progress = y / (double) height;
            edc.updProg("nonMax suppression ", progress);
        }
    }

    private int thresholding(int magnitude) {
        if (magnitude>mainTRESHOLD) return magnitude;
        else return 0;
    }

    private void doGaussian() {
        int dyk = kernel/2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // ziskej okoli pro gauss
                int[][] blur = new int[kernel][kernel];
                for (int j = 0; j < kernel; j++) {
                    for (int i = 0; i < kernel; i++) {
                        if (x<dyk || y <dyk || x> width-dyk-1|| y>height-dyk-1) blur[i][j] = rawImage.getRGB(x, y) & 0xFF;
                        else blur[i][j] = rawImage.getRGB(x - dyk + i, y - dyk + j) & 0xFF;
                    }
                }

                // rozmazat obrazek
                int blurred = 0;
                for (int j = 0; j < kernel; j++) {
                    for (int i = 0; i < kernel; i++) {
                        blurred += blur[i][j] * conv1[i][j];
                    }
                }
                int magnitude = truncate((int)(blurred*(1/getKernelSum(conv1))));
                int gs = (magnitude<<16)+(magnitude<<8)+magnitude;
                blurred_img.setRGB(x, y, gs);
            }
            double progress = y / (double) height;
            edc.updProg("Blurring ", progress);
        }
    }

    private void setSobelConv(){
        conv1 = new double[][]{
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
        conv2 = new double[][]{
                {1, 2, 1},
                {0, 0, 0},
                {-1, -2, -1}
        };
    }

    public double[][] getGausMatrix(int size) {
        double[][] mat = new double[][]{};
        if (size == 3) {
            mat = new double[][]{
                    {1, 2, 1},
                    {2, 4, 2},
                    {1, 2, 1}
            };
        } else if (size == 5) {
            mat = new double[][]{
                    {1, 4, 6, 4, 1},
                    {4, 16, 24, 16, 4},
                    {6, 24, 36, 24, 6},
                    {4, 16, 24, 24, 4},
                    {1, 4, 6, 4, 1}
            };
        } else if (size == 7) {
            mat = new double[][]{
                    {1, 4, 7, 10, 7, 4, 1},
                    {4, 12, 26, 33, 26, 12, 4},
                    {7, 26, 55, 71, 55, 26, 7},
                    {10, 33, 71, 91, 71, 33, 10},
                    {7, 26, 55, 71, 55, 26, 7},
                    {4, 12, 26, 33, 26, 12, 4},
                    {1, 4, 7, 10, 7, 4, 1}
            };
        }

        return mat;
    }

    public BufferedImage getOutput_img() {
        return output_img;
    }
}