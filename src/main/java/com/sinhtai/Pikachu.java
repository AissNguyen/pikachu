package com.sinhtai;

import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

public class Pikachu {
    private static final int gameOriginX = 277;
    private static final int gameOriginY = 110;
    public static void main(String[] args) {
        try {

            Robot robot = new Robot();


            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle rect = new Rectangle(gameOriginX, gameOriginY, 12* ImageProcessing.width, 8* ImageProcessing.height);
            BufferedImage screenFullImage = robot.createScreenCapture(rect);
            String gameScreenImage = "E:/git/taint/pikachu/game.jpg";
            ImageIO.write(screenFullImage, "jpg", new File(gameScreenImage));
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            screenFullImage = robot.createScreenCapture(screenRect);
            ImageIO.write(screenFullImage, "jpg", new File("E:/git/taint/pikachu/screenshot.jpg"));

            ImageProcessing imageProcessing = new ImageProcessing();
            Mat[][] splitImages = imageProcessing.splitImageAndSave(gameScreenImage);
            imageProcessing.processImage(splitImages, true);


            Map<String, String> steps = null;
            for (int i = 0; i < 2; i++) {
                if(imageProcessing.isDone()){
                    break;
                }
                steps = imageProcessing.playGame();
            }
            System.out.println("matrix before solve:");
            imageProcessing.printMatrix();
            System.out.println("matrix after solve:");
            imageProcessing.printMatrix();
            for (String step: steps.keySet()){
                System.out.println(step + " and " + steps.get(step));
                String source = step;
                String target = steps.get(step);
                int sourceRowIndex = Integer.parseInt(source.split("_")[0]);
                int sourceColumnIndex = Integer.parseInt(source.split("_")[1]);

                int targetRowIndex = Integer.parseInt(target.split("_")[0]);
                int targetColumnIndex = Integer.parseInt(target.split("_")[1]);

                click(robot, sourceRowIndex, sourceColumnIndex);
                click(robot, targetRowIndex, targetColumnIndex);

                //break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    public static void click(Robot robot, int rowIndex, int colIndex){
        int x = colIndex*ImageProcessing.width + gameOriginX + ImageProcessing.width/2;
        int y = rowIndex*ImageProcessing.height + gameOriginY + ImageProcessing.height/2;
        robot.mouseMove(x,y);
        System.out.println(x+ "," + y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}