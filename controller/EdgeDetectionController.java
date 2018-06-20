package controller;

import methods.Canny;
import methods.Laplace;
import methods.Marr_Hildereth;
import methods.Masks;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Lukado on 23. 11. 2016.
 */
public class EdgeDetectionController extends Thread {
    private String rawImageFilePath, extension;
    private GuiController gc;

    EdgeDetectionController(GuiController gc, String rawImageFilePath, String extension) {
        this.gc = gc;
        this.rawImageFilePath = rawImageFilePath;
        this.extension = extension.toLowerCase();
    }

    @Override
    public void run(){
        String outputImageFilePath = "img/result."+extension;
        String grayedImageFilePath = "img/gray."+extension;
        ImageController il = new ImageController();
        BufferedImage image = il.readImage(rawImageFilePath);

        image = grayOut(image);
        il.writeImage(image, grayedImageFilePath, extension);
        gc.updateImg(grayedImageFilePath);

        image = executeMethod(image);

        updProg("Ukládání obrázku ");
        il.writeImage(image, outputImageFilePath, extension);
        updProg("", 1);
        gc.updateImg(outputImageFilePath);
    }

    private BufferedImage executeMethod(BufferedImage image) {
        int filter = 0, dir;
        switch (gc.cBox.getSelectionModel().getSelectedIndex()){
            case 0:
                if (gc.rbm1.isSelected()) filter = 1;
                else if (gc.rbm2.isSelected()) filter = 2;
                else if (gc.rbm3.isSelected()) filter = 3;
                else if (gc.rbm4.isSelected()) filter = 4;

                dir = getDirection();

                image = new Masks(image, filter, dir, gc.gradCheckBox.isSelected(), gc.threshCheckBox.isSelected(), this).getImg();
                break;
            case 1:
                if (gc.rbp1.isSelected()) {
                    switch (gc.cBoxLap.getSelectionModel().getSelectedIndex()){
                        case 0:
                            filter = 8;
                            break;
                        case 1:
                            filter = 4;
                    }
                }
                else if (gc.rbp2.isSelected()) filter = 6;

                dir = getDirection();

                image = new Laplace(image, filter, dir, gc.gradCheckBox.isSelected(), gc.threshCheckBox.isSelected(), this).getImg();
                break;
            case 2:
                if (gc.rbs1.isSelected()) image = new Canny(image, Integer.parseInt(gc.gaussKernelTF.getText()), Double.parseDouble(gc.gaussSigmaTF.getText()),
                        Double.parseDouble(gc.cannyLowerThreshTF.getText()), Double.parseDouble(gc.cannyUpperThreshTF.getText()), this).getOutput_img();
                else if (gc.rbs2.isSelected()) image = new Marr_Hildereth(image, Integer.parseInt(gc.gaussKernelTF.getText()), Double.parseDouble(gc.gaussSigmaTF.getText()), this).getOutput_img();
        }
        return image;
    }

    private int getDirection() {
        switch (gc.cBoxDir.getSelectionModel().getSelectedIndex()){
            case 0:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 1:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 2:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 3:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 4:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 5:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 6:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 7:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
            case 8:
                return gc.cBoxDir.getSelectionModel().getSelectedIndex()-1;
        }
        return -1;
    }

    private BufferedImage grayOut(BufferedImage img) {
        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int val = img.getRGB(i,j);
                int grayLevel;
                if (gc.maskCheckBox.isSelected()) grayLevel = (int) (255-((val >> 16 & 0xFF) * 0.2126 + (val >> 8 & 0xFF) * 0.7152 + (val & 0xFF) * 0.0722));
                else grayLevel = (int) (((val >> 16 & 0xFF) * 0.2126 + (val >> 8 & 0xFF) * 0.7152 + (val & 0xFF) * 0.0722));
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;

//                Color c = new Color(img.getRGB(i, j));
//                int red = (int) (c.getRed());
//                int green = (int) (c.getGreen());
//                int blue = (int) (c.getBlue());
//                int gray = (red+green+blue)/3;
//                Color newColor = new Color(
//                        gray,
//                        gray,
//                        gray);
                newImage.setRGB(i, j, gray);
            }
        }
        return newImage;
    }

    public void updProg(String text){
        updProg(text, gc.progB.getProgress());
    }

    public void updProg(double value){
        updProg("", value);
    }

    public void updProg(String text, double value) {
        gc.updateProgress(text, value);
    }
}
