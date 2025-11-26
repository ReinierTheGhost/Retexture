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
    private VBox previewBox;
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

        Button loadColorImageButton = new Button("Load Color Image");
        loadColorImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
            File colorImage = fileChooser.showOpenDialog(null);
            if (colorImage != null) {
                extractColorsFromImage(colorImage);
            }
        });
        previewBox = new VBox(5);
        Label previewLabel = new Label("Selected Color Preview:");


        colorBox = new VBox(5);
        addColorsPicker();
        VBox colorControls = new VBox(10,
                new Label("Custom Colors:"),
                colorBox,
                addColorsButton,
                loadColorImageButton,
                processButton,
                saveButton
        );

        HBox mainContent = new HBox(30,
                colorControls,
                processedView
        );
        processedView.setFitWidth(300);
        processedView.setPreserveRatio(true);

        VBox root = new VBox(15,
                loadButton,
                originalView,
                mainContent // this replaces the old VBox with preview
        );
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
    private void extractColorsFromImage(File imageFile) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            List<java.awt.Color> extractedColors = TextureProcessor.extractUniqueColors(img, 10); // max 10 unique colors

            for (java.awt.Color awtColor : extractedColors) {
                Color fxColor = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
                ColorPicker picker = new ColorPicker(fxColor);
                TextField hexField = new TextField(String.format("#%02X%02X%02X", awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
                colorPickers.add(picker);
                hexFields.add(hexField);

                HBox row = new HBox(10, new Label("Color:"), picker, new Label("Hex:"), hexField);
                row.setPadding(new Insets(5));
                colorBox.getChildren().add(row);

                addPreviewColor(fxColor);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPreviewColor(Color color) {
        Label preview = new Label();
        preview.setPrefSize(30, 30);
        preview.setStyle("-fx-background-color: " + toHexString(color) + "; -fx-border-color: black;");
        previewBox.getChildren().add(preview);
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}
