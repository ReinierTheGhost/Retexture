package com.legends;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    public static List<Color> extractUniqueColors(BufferedImage image, int maxColors) {
        Set<Integer> colorSet = new LinkedHashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                colorSet.add(rgb);
                if (colorSet.size() >= maxColors) break;
            }
            if (colorSet.size() >= maxColors) break;
        }

        List<Color> colorList = new ArrayList<>();
        for (int rgb : colorSet) {
            java.awt.Color c = new java.awt.Color(rgb);
            colorList.add(c);
        }
        return colorList;
    }

}
