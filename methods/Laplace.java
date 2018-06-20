package methods;

import controller.EdgeDetectionController;

import java.awt.image.BufferedImage;

public class Laplace extends EdgeMath {
    private BufferedImage output_img;
    private int[][] conv1, conv2;

    public Laplace(BufferedImage img, int o, int dir, boolean grad, boolean thresh, EdgeDetectionController edc) {
        int width = img.getWidth();
        int height = img.getHeight();
        output_img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        setFilter(o, dir);

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
                        if (o == 6 && dir == -1) {
                            gray1 += gray[i][j] * conv1[i][j];
                            gray2 += gray[i][j] * conv2[i][j];
                        } else gray1 += gray[i][j] * conv1[i][j];
                    }
                }

                int magnitude, gs;
                double gradient = 0;
                if (dir==-1 && o == 6) {
                    if (thresh) magnitude = truncate((int) (Math.sqrt(gray1*gray1 + gray2*gray2)*(255/(255.0*o))));
                    else magnitude = truncate((int) (Math.sqrt(gray1*gray1 + gray2*gray2)));

                    if (gray1!=0) gradient = Math.atan(gray2/gray1);
                }
                else {
                    if (thresh) magnitude = truncate((int)((Math.round(gray1)*(255/(255.0*o)))));
                    else magnitude = truncate(Math.round(gray1));

                    gradient = Math.atan(gray1);
                }

                if (grad) gs = ((int)(magnitude*Math.sin(gradient))<<8)+((int)(magnitude*Math.cos(gradient))<<16);
                else gs = (magnitude<<16)+(magnitude<<8)+magnitude;
                output_img.setRGB(x, y, gs);
            }
            double progress = y/(double)height;
            edc.updProg(progress);
            System.out.println();
        }
    }

    private void setFilter(int o, int dir){
        if (o == 4) {
            conv1 = new int[][]{
                    {0, -1, 0},
                    {-1, 4, -1},
                    {0, -1, 0}
            };
        }
        else if (o == 8){
            conv1 = new int[][]{
                    {-1, -1, -1},
                    {-1, 8, -1},
                    {-1, -1, -1}
            };
        }
        else{
            if (dir!=-1) {
                conv1 = new int[][]{
                        {-1, 2, -1},
                        {-1, 2, -1},
                        {-1, 2, -1}
                };
                if (dir%2==1) dir+=2;
                conv1 = rotateConv(conv1, dir%8);
            }else {
                conv1 = new int[][]{
                        {-1, 2, -1},
                        {-1, 2, -1},
                        {-1, 2, -1}
                };
                conv2 = new int[][]{
                        {-1, -1, -1},
                        {2, 2, 2},
                        {-1, -1, -1}
                };
            }
        }
    }

    public BufferedImage getImg() {
        return output_img;
    }
}
