package com.legends;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class TextureProcessor {

    public static BufferedImage retexture(BufferedImage image, List<Color> palette){
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int rgb = image.getRGB(x, y);
                Color originalColor = new Color(rgb, true);
                Color newColor = findClossestColor(originalColor, palette);
                newImage.setRGB(x, y, newColor.getRGB());
            }
        }
        return newImage;
    }

    private static Color findClossestColor(Color original, List<Color> palette){
        Color closest = palette.get(0);
        double minDist = colorDistance(original, closest);

        for (Color color : palette){
            double distance = colorDistance(original, color);
            if (distance < minDist){
                minDist = distance;
                closest = color;
            }
        }
        return closest;
    }

    private static double colorDistance(Color c1, Color c2) {
        int rDiff = c1.getRed() - c2.getRed();
        int gDiff = c1.getGreen() - c2.getGreen();
        int bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }
}
