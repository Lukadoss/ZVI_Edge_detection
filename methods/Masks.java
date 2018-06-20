package methods;

import controller.EdgeDetectionController;

import java.awt.image.*;

public class Masks extends EdgeMath {
    private BufferedImage output_img;
    private int[][] conv1, conv2;
    private int o;

    public Masks(BufferedImage img, int filter, int dir, boolean grad, boolean thresh, EdgeDetectionController edc) {
        int width = img.getWidth();
        int height = img.getHeight();
        output_img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        switch (filter){
            case 1:
                prewitt(dir);
                break;
            case 2:
                sobel(dir);
                break;
            case 3:
                robinson(dir);
                break;
            case 4:
                kirsch(dir);
        }

        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {

                // ziskej okoli
                int[][] gray = new int[3][3];
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 3; i++) {
                        gray[i][j] = img.getRGB(x-1+i,y-1+j) & 0xFF;
                    }
                }

                // aplikuj filtr
                int gray1 = 0, gray2 = 0;
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 3; i++) {
                        if (dir != -1) gray1 += gray[i][j] * conv1[i][j];
                        else {
                            gray1 += gray[i][j] * conv1[i][j];
                            gray2 += gray[i][j] * conv2[i][j];
                        }
                    }
                }
                int magnitude, gs;
                double gradient =0;
                if (dir!=-1) {
                    if (thresh) magnitude = truncate((int)((Math.abs(gray1)*(255/(255.0*o)))));
                    else magnitude = truncate((Math.abs(gray1)));

                    gradient = Math.atan(gray1);
                }
                else {
                    if (thresh) magnitude = truncate((int) (Math.sqrt(gray1*gray1 + gray2*gray2)*(255/(255.0*o))));
                    else magnitude = truncate((int) (Math.sqrt(gray1*gray1 + gray2*gray2)));

                    if (gray1!=0) gradient = Math.atan(gray2/gray1);
                }

                if (grad) gs = ((int)(magnitude*Math.sin(gradient))<<8)+((int)(magnitude*Math.cos(gradient))<<16);
                else gs = (magnitude<<16)+(magnitude<<8)+magnitude;

                output_img.setRGB(x, y, gs);
            }
            double progress = y/(double)height;
            edc.updProg(progress);
        }
    }
    public BufferedImage getImg(){
        return output_img;
    }

    private void prewitt(int dir){
        if (dir!=-1) {
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-1, 0, 1},
                    {-1, 0, 1}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else{
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-1, 0, 1},
                    {-1, 0, 1}
            };
            conv2 = new int[][]{
                    {1, 1, 1},
                    {0, 0, 0},
                    {-1, -1, -1}
            };
        }
        o = 3;
    }

    private void sobel(int dir){
        if (dir!=-1){
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-2, 0, 2},
                    {-1, 0, 1}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else {
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-2, 0, 2},
                    {-1, 0, 1}
            };
            conv2 = new int[][]{
                    {1, 2, 1},
                    {0, 0, 0},
                    {-1, -2, -1}
            };
        }
        o = 4;
    }

    private void robinson(int dir){
        if (dir!=-1){
            conv1 = new int[][]{
                    {-1, 1, 1},
                    {-1, -2, 1},
                    {-1, 1, 1}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else{
            conv1 = new int[][]{
                    {-1, 1, 1},
                    {-1, -2, 1},
                    {-1, 1, 1}
            };
            conv2 = new int[][]{
                    {1, 1, 1},
                    {1, -2, 1},
                    {-1, -1, -1}
            };
        }
        o = 5;
    }

    private void kirsch(int dir){
        if (dir!=-1){
            conv1 = new int[][]{
                    {-5, 3, 3},
                    {-5, 0, 3},
                    {-5, 3, 3}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else {
            conv1 = new int[][]{
                    {-5, 3, 3},
                    {-5, 0, 3},
                    {-5, 3, 3}
            };
            conv2 = new int[][]{
                    {3, 3, 3},
                    {3, 0, 3},
                    {-5, -5, -5}
            };
        }
        o = 15;
    }


}
