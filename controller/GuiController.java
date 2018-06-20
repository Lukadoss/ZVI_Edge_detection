package controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Lukado on 23. 11. 2016.
 */
public class GuiController {
    public BorderPane mainStage;
    public Button picBut, solve;
    public ImageView ivIN, ivOUT, ivGS;
    public Pane pnIN, pnOUT, pnGS;
    public Label picWarning, progT;
    public ProgressBar progB;
    public ChoiceBox<String> cBox, cBoxLap, cBoxDir;
    public RadioButton rbm1, rbm2, rbm3, rbm4, rbp1, rbp2, rbs1, rbs2;
    public AnchorPane a1, a2, a3, a4, a5;
    public CheckBox gradCheckBox, threshCheckBox, maskCheckBox;
    public TextField gaussKernelTF, gaussSigmaTF, cannyLowerThreshTF, cannyUpperThreshTF;

    private String filePath, ext;
    private Desktop desktop = Desktop.getDesktop();
    private Image img;
    private double aspectRatio;

    @FXML
    public void initialize() {
        if(ivIN !=null && ivOUT != null && ivGS != null) {
            ivIN.fitWidthProperty().bind(pnIN.widthProperty());
            ivIN.fitHeightProperty().bind(pnIN.heightProperty());
            ivOUT.fitWidthProperty().bind(pnOUT.widthProperty());
            ivOUT.fitHeightProperty().bind(pnOUT.heightProperty());
            ivGS.fitWidthProperty().bind(pnGS.widthProperty());
            ivGS.fitHeightProperty().bind(pnGS.heightProperty());
            listenResize();

            cBox.setItems(FXCollections.observableArrayList("Metody masek","Detekce čáry a bodu", "Speciální hranové detektory"));
            cBox.setValue("Metody masek");
            cBoxLap.setItems(FXCollections.observableArrayList("8-okolí", "4-okolí"));
            cBoxLap.setValue("8-okolí");
            cBoxDir.setItems(FXCollections.observableArrayList("Žádný", "0", "1", "2", "3", "4", "5", "6", "7"));
            cBoxDir.setValue("Žádný");
            listenSelectedBoxItem();

            final ToggleGroup mgr = new ToggleGroup(); // mask group
            rbm1.setToggleGroup(mgr);
            rbm2.setToggleGroup(mgr);
            rbm3.setToggleGroup(mgr);
            rbm4.setToggleGroup(mgr);
            final ToggleGroup pgr = new ToggleGroup(); // point group
            rbp1.setToggleGroup(pgr);
            rbp2.setToggleGroup(pgr);
            final ToggleGroup sgr = new ToggleGroup(); // special group
            rbs1.setToggleGroup(sgr);
            rbs2.setToggleGroup(sgr);
            listenSelectedRadioItem(pgr);
            listenSelectedRadioItem(sgr);

            listenTextBoxes();
        }
    }


    public void solve(ActionEvent actionEvent) {
        if (filePath != null) {
            if (gaussKernelTF.getText().equals("") || Double.parseDouble(gaussKernelTF.getText())<2) gaussKernelTF.setText("3");
            if (gaussSigmaTF.getText().equals("")) gaussSigmaTF.setText("1");
            if (cannyLowerThreshTF.getText().equals("")) gaussKernelTF.setText("0.3");
            if (cannyUpperThreshTF.getText().equals("")) gaussSigmaTF.setText("0.7");


            Thread edc = new EdgeDetectionController(this, filePath, ext);
            edc.start();
        }else if (!picWarning.isVisible()){
            picWarning.setVisible(true);
        }
    }

    public void openFile(ActionEvent actionEvent) {
        Stage stage = (Stage) mainStage.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Picture loader");
        fileChooser.setInitialDirectory(
//                new File(System.getProperty("user.home"))
                new File(".")
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG, PNG, BMP", "*.jpg", "*.png", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            picWarning.setVisible(false);
            filePath = file.getPath();
            img = new Image(file.toURI().toString());

            ivIN.setImage(img);
            ivOUT.setImage(null);
            ivGS.setImage(null);
            progB.setProgress(0);
            progT.setText("");

            aspectRatio = img.getWidth() / img.getHeight();
            double imgHeight = Math.min(ivIN.getFitHeight(), ivIN.getFitWidth() / aspectRatio);
            double imgWidth = Math.min(ivIN.getFitWidth(), ivIN.getFitHeight() * aspectRatio);

            ivIN.setX(pnIN.getWidth() / 2 - imgWidth / 2);
            ivIN.setY(pnIN.getHeight() / 2 - imgHeight / 2);
            ivIN.setImage(new Image(file.toURI().toString()));

            picBut.setText(file.getName());
            ext = picBut.getText().substring(picBut.getText().lastIndexOf(".")+1);
        }

    }

    public void openImage(MouseEvent mouseEvent) {
        if (ivIN != null) {
            switch (mouseEvent.getPickResult().getIntersectedNode().getId()){
                case "ivIN":
                    if (ivIN.getImage() != null){
                        try {
                            desktop.open(new File(filePath));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                case "ivGS":
                    if (ivGS.getImage() != null){
                        try {
                            desktop.open(new File("img/gray."+ext.toLowerCase()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                case "ivOUT":
                    if (ivOUT.getImage() != null){
                        try {
                            desktop.open(new File("img/result."+ext.toLowerCase()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    }

    public void about(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../about.fxml"));
            Stage stage = new Stage();
            stage.setTitle("O programu");
            stage.setScene(new Scene(root, 350, 125));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenResize() {
        resizeHW(pnIN, ivIN);
        resizeHW(pnOUT, ivOUT);
        resizeHW(pnGS, ivGS);
    }

    private void listenTextBoxes() {
        listenTextBox(gaussKernelTF);
        listenTextBox(gaussSigmaTF);
        listenTextBox(cannyLowerThreshTF);
        listenTextBox(cannyUpperThreshTF);
    }

    private void listenSelectedBoxItem() {
        cBox.valueProperty().addListener(observable -> {
            resetAnchors();
            switch (cBox.getSelectionModel().getSelectedIndex()){
                case 0:
                    a1.setVisible(true);
                    break;
                case 1:
                    a2.setVisible(true);
                    if (rbp1.isSelected()) a4.setDisable(true);
                    else a4.setDisable(false);
                    break;
                case 2:
                    a3.setVisible(true);
                    a4.setVisible(false);
                    a4.setMinHeight(0);
                    a4.setPrefHeight(0);
                    a5.setVisible(true);
            }
        });
    }

    private void listenSelectedRadioItem(ToggleGroup gr) {
        gr.selectedToggleProperty().addListener(observable -> {
            if (rbp1.isSelected()) {
                cBoxLap.setDisable(false);
                a4.setDisable(true);
            } else {
                cBoxLap.setDisable(true);
                a4.setDisable(false);
            }

            if (rbs1.isSelected()){
                cannyLowerThreshTF.setDisable(false);
                cannyUpperThreshTF.setDisable(false);
            }
            else {
                cannyLowerThreshTF.setDisable(true);
                cannyUpperThreshTF.setDisable(true);
            }
        });
    }

    private void listenTextBox(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d{0,7}([.]\\d{0,4})?")) {
                    textField.setText(oldValue);
                }
                if (textField.getId().equals("gaussKernelTF") && isParsable(textField.getText())) {
                    if (Integer.parseInt(textField.getText()) % 2 == 0)
                        Platform.runLater(textField::clear);
                } else if (textField.getId().equals("gaussKernelTF")) {
                    Platform.runLater(textField::clear);
                }
                if ((textField.getId().equals("cannyLowerThreshTF") || textField.getId().equals("cannyUpperThreshTF")) && isParsable(textField.getText())) {
                    if (Double.parseDouble(textField.getText()) > 1 || Double.parseDouble(textField.getText()) < 0){
                        Platform.runLater(textField::clear);
                    }
                }
            }
        });
    }

    void updateImg(String s) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (s.contains("gray")) {
                    fitImage(ivGS, pnGS, s);
                }else{
                    fitImage(ivOUT, pnOUT, s);
                }
            }
        });
    }

    void updateProgress(String text, double val){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progB.setProgress(val);
                progT.setText(text+Math.round(val * 100) + " %");
            }
        });
    }

    private void resizeHW(Pane tmpPN, ImageView tmpIV) {
        tmpPN.heightProperty().addListener(observable -> {
            double imgWidth = Math.min(tmpIV.getFitWidth(), tmpIV.getFitHeight() * aspectRatio);
            double imgHeight = Math.min(tmpIV.getFitHeight(), tmpIV.getFitWidth() / aspectRatio);
            if (img != null) {
                tmpIV.setX(tmpPN.getWidth() / 2 - imgWidth / 2);
                tmpIV.setY(tmpPN.getHeight() / 2 - imgHeight / 2);
            }
        });

        tmpPN.widthProperty().addListener(observable -> {
            double imgWidth = Math.min(tmpIV.getFitWidth(), tmpIV.getFitHeight() * aspectRatio);
            double imgHeight = Math.min(tmpIV.getFitHeight(), tmpIV.getFitWidth() / aspectRatio);
            if (img != null) {
                tmpIV.setX(tmpPN.getWidth() / 2 - imgWidth / 2);
                tmpIV.setY(tmpPN.getHeight() / 2 - imgHeight / 2);
            }
        });
    }

    private void fitImage(ImageView iv, Pane pn, String s){
        double imgHeight = Math.min(iv.getFitHeight(), iv.getFitWidth() / aspectRatio);
        double imgWidth = Math.min(iv.getFitWidth(), iv.getFitHeight() * aspectRatio);

        iv.setImage(new Image("file:" + s));
        iv.setX(pn.getWidth() / 2 - imgWidth / 2);
        iv.setY(pn.getHeight() / 2 - imgHeight / 2);
    }

    private void resetAnchors() {
        a1.setVisible(false);
        a2.setVisible(false);
        a3.setVisible(false);
        a4.setVisible(true);
        a5.setVisible(false);

        a4.setDisable(false);
        a4.setMinHeight(Region.USE_COMPUTED_SIZE);
        a4.setPrefHeight(Region.USE_COMPUTED_SIZE);
    }

    private static boolean isParsable(String input) {
        boolean parsable = true;
        try {
            Double.parseDouble(input);
        } catch (NumberFormatException e) {
            parsable = false;
        }
        return parsable;
    }
}
