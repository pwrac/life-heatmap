/*
Author: Aidan Clark

Description: A program that generates an image of a heatmap based on the 
frequency that a pixel is landed on during an instance of Conway's game of life

Final Project for CS160: Structured Programming at UNCO

THIS PROJECT USES JAVA OPENJDK11
*/

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class GOLHeatMap {

    // Initialize key variables, as fallback for bad inputs
    protected static int width = 400, height = 300, baseDepth = 255, currDepth = 0;
    protected static short[][] imgArray = null;
    protected static boolean[][] lifeArray = null; // True indicates alive cell, false indicates dead cell
    
    // Generates the first generation of lifeArray
    // Randomly sets indices with either true or false
    static void makeFirstGeneration() {
        Random rand = new Random(42); // Seed of 42 for testing
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                lifeArray[y][x] = (Math.round(rand.nextFloat()) == 1) ? true : false;
            }
        }
        rand = null;
        updateImageArray(true);
        System.out.println("Initialized image...");
        currDepth++;
    }
    
    // Generates the next generation using Conway's Game of Life Rules
    static void nextGeneration() {
        
        boolean[][] nextLiving = new boolean[height][width];
        
        // Marching on each cell, and determine which cells live on next gen
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
            
                // Find alive cells directly around current cell
                // Using max and min, indices stay in-bounds
                // Ex: At y = 0, it compares 0 and y-1 = -1. 0 is the max, staying in-bounds
                int livingNeighbors = 0;
                for (int row = -1; row <= 1; row++) {
                    for (int col = -1; col <= 1; col++) {
                        if (row == 0 && col == 0) continue; // Don't count self
                        if ((y + row < 0) || (y + row >= height)) continue; // Avoid vertical out-of-bounds
                        if ((x + col < 0) || (x + col >= width)) continue; // Avoid horizontal out-of-bounds
                        
                        if (lifeArray[y + row][x + col] == true) livingNeighbors++;
                    }
                }
                //System.out.printf("Cell (%d, %d) has %d neighbors\n", x, y, livingNeighbors);
                
                // Apply Game of Life Rules
                // 1. Live cells with 2 or 3 neighbors survives
                // 2. Dead cells with 3 neighbors revives
                // 3. Any other live cell dies, and dead cells stay dead
                if (lifeArray[y][x] == true && (livingNeighbors == 2 || livingNeighbors == 3)) {
                    nextLiving[y][x] = true;
                }
                else if (lifeArray[y][x] == false && (livingNeighbors == 3)) {
                    nextLiving[y][x] = true;
                }
                else {
                    nextLiving[y][x] = false;
                }
            }
        }
        
        // System.out.println("Generated gen " + currDepth);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                lifeArray[y][x] = nextLiving[y][x];
            }
        }
        
        nextLiving = null;
        
        updateImageArray(true);
        
        currDepth++;
    }
    
    // Adds 1 to each parallel index in imgArray where a cell is alive within lifeArray
    static void updateImageArray(boolean isHeatMap) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (!isHeatMap) imgArray[i][j] = (lifeArray[i][j]) ? (short) 1 : (short) 0;
                else imgArray[i][j] += (lifeArray[i][j]) ? (short) 1 : (short) 0;
            }
        }
    }
    
    // Sets the luminance values (RGB) for each pixel using the imgArray
    // Strength is used for testing, and should be 1 for most cases; increases luminence for each gen
    static void drawFromArray(BufferedImage image, int strength) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tmpValue = (int) imgArray[y][x] * strength;
                if (tmpValue > 255) tmpValue = 255;
                
                // Set rgb with same values to emulate grayscale, alpha with 255 to allow
                int argbValue = ( (0 << 24) | (tmpValue << 16) | (tmpValue << 8) | tmpValue );
                
                image.setRGB(x, y, argbValue);
            }
        }
        System.out.println("Heatmap Generated! Writing to file...");
    }
    
    // Main
    public static void main(String[] args) throws IOException {
        
        // If arguments are passed, set baseDepth, width, and height
        switch (args.length) {
			case 3: // Set width, height, depth
				width = Integer.parseInt(args[0]);
				height = Integer.parseInt(args[1]);
				baseDepth = Integer.parseInt(args[2]);
				break;
			case 2: // Set dimensions
				width = Integer.parseInt(args[0]);
				height = Integer.parseInt(args[1]);
				break;
			case 1: // Set baseDepth
				baseDepth = Integer.parseInt(args[0]);
				break;
			default:
				System.out.println("No arguments passed, or arguments exceeded 3. Using default values");
				break;
		}
        
        // Setting limits and catching bad inputs
                
        // Warn user for large files, exit if larger than 8K
        if ((long) (width * height) >= (long) 8294400) {
            // Limit to 8k resolutions and lower
            if ((long) (width * height) > (long) 33177600) {
                System.out.println("Dimensions are limited to a max amount of pixels equal to 8K resolutions\nExiting...");
                System.exit(1);
            }
            
            // Prompt user about time and CPU thread usage for larger files
            Scanner find = new Scanner(System.in);
            System.out.println("The dimensions entered are equal to or larger than 4K. This may use lots of CPU usage and time.\nContinue? [y/N]");
            String tmpString = find.nextLine().toLowerCase();
            
            if (tmpString.charAt(0) == 'y'); // Strictly allow only if it starts with y
            else {
                // Default to exiting
                System.out.println("Exiting...");
                System.exit(1);
            }
            find = null;
            
        }
        
        // Set dim minimum
        if (width < 5 || height < 5) {
            System.out.println("Please make sure that either of your dimensions are at least 5 pixels");
            System.exit(1);
        }
        
        // Set minimum and maximum for the depth (0-255)
        if ((baseDepth > 255) || (baseDepth < 0)) {
            System.out.println(baseDepth + " was input as the depth. Depth is limited to 0-255 for this program.\nExiting...");
            System.exit(1);
        }
        
        // Tell user what inputs they entered
        System.out.printf("Using values width=%d height=%d depth=%d\n", width, height, baseDepth);
        
        // Initialize image Array and the Game of Life's Array
        imgArray = new short[height][width];
        lifeArray = new boolean[height][width];
        
        // Default null for garbage collection in case of error
        BufferedImage image = null;
        File outFile = null;
        
        try {
            // Initialize BufferedImage to individually set pixel RGB values
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            // Make the first generation (gen 0)
            makeFirstGeneration();
            
            while (currDepth <= baseDepth) nextGeneration();
            System.out.println("Max depth reached. Generating image...");
            
            drawFromArray(image, 1);
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
            Date timeDate = new Date();
            outFile = new File("Images/" + timeFormat.format(timeDate) + ".png"); // Ex: 09-12-2022-12:53:22.png
            
            // Generates and writes the image to file path, using .png format as it's lossless
            ImageIO.write(image, "png", outFile);
            
            System.out.println("[SUCCESS]\nImage written to \"" + outFile.getAbsolutePath() +"\"");
        }
        catch (IOException errorMsg) {
            System.out.println("[FAILURE] IOException Caught\n" + errorMsg);
        }
        finally {
            // Leave BufferedImage and File objects for garbage collection
            if (image != null) image = null;
            if (outFile != null) outFile = null;
        }
    }
}