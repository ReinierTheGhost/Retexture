package com.legends;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextureApp extends Application {

    private ImageView originalView;
    private ImageView processedView;
    private File selectedFile;
    private VBox colorBox;
    private List<ColorPicker> colorPickers = new ArrayList<>();
    private List<TextField> hexFields = new ArrayList<>();

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Texture Retexturing Tool");

        originalView = new ImageView();
        processedView = new ImageView();
        originalView.setFitWidth(256);
        originalView.setPreserveRatio(true);
        processedView.setFitWidth(256);
        processedView.setPreserveRatio(true);

        Button loadButton = new Button("Load Texture");
        loadButton.setOnAction(e -> loadTexture());

        Button addColorsButton = new Button("Add Color");
        addColorsButton.setOnAction(e -> addColorsPicker());

        Button processButton = new Button("Apply Colors");
        processButton.setOnAction(e -> applyRetexturing());

        Button saveButton = new Button("Save Image");
        saveButton.setOnAction(e -> saveRetexturedImage());

        colorBox = new VBox(5);
        addColorsPicker();

        VBox root = new VBox(10, loadButton, originalView, new Label("Custom Colors:"), colorBox, addColorsButton, processButton, saveButton, processedView);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 900);
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    private void loadTexture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null){
            originalView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }
    private void addColorsPicker() {
        ColorPicker colorPicker = new ColorPicker();
        TextField hexField = new TextField();
        hexField.setPromptText("#RRGGBB");
        colorPickers.add(colorPicker);
        hexFields.add(hexField);

        // Sync ColorPicker -> HexField
        colorPicker.setOnAction(e -> {
            Color color = colorPicker.getValue();
            String hex = String.format("#%02X%02X%02X",
                    (int)(color.getRed() * 255),
                    (int)(color.getGreen() * 255),
                    (int)(color.getBlue() * 255));
            hexField.setText(hex);
        });

        HBox colorRow = new HBox(10, new Label("Color:"), colorPicker, new Label("Hex:"), hexField);
        colorRow.setPadding(new Insets(5));
        colorBox.getChildren().add(colorRow);
    }
    private void applyRetexturing() {
        if (selectedFile == null) return;

        try {
            BufferedImage image = ImageIO.read(selectedFile);

            List<java.awt.Color> palette = new ArrayList<>();
            for (int i = 0; i < colorPickers.size(); i++) {
                Color fxColor = parseColorInput(hexFields.get(i).getText(), colorPickers.get(i).getValue());
                java.awt.Color awtColor = new java.awt.Color(
                        (float) fxColor.getRed(),
                        (float) fxColor.getGreen(),
                        (float) fxColor.getBlue()
                );
                palette.add(awtColor);
            }

            System.out.println("Selected Colors:");
            for (java.awt.Color c : palette){
                System.out.println("R:" + c.getRed() + " G:" + c.getGreen() + " B:" + c.getBlue());
            }

            BufferedImage newImage = TextureProcessor.retexture(image, palette);

            File output = new File("temp_texture.png");
            ImageIO.write(newImage, "png", output);

            processedView.setImage(new Image(output.toURI().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Color parseColorInput(String hex, Color fallback) {
        if (hex != null && !hex.isEmpty()) {
            try {
                hex = hex.trim().replace("#", "");
                if (hex.matches("(?i)[0-9a-f]{6}")) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    return Color.rgb(r, g, b);
                }
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    private void saveRetexturedImage(){
        if (processedView.getImage() == null){
            System.out.println("No retextured image to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG image", "*.png"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG image", "*.jpg"));

        fileChooser.setTitle("Save Image");

        File file = fileChooser.showSaveDialog(null);
        if (file != null){
            try {
                BufferedImage image = ImageIO.read(new File("temp_texture.png"));

                String format = file.getName().endsWith(".jpg") ? "jpg" : "png";

                ImageIO.write(image, format, file);
                System.out.println("Image saved: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
