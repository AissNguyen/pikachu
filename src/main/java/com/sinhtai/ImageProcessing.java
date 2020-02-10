package com.sinhtai;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class ImageProcessing {
    static {
        //Loading the core library
        nu.pattern.OpenCV.loadShared();
        // System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    Map<Integer, Mat> imageMap = new HashMap<>();
    //static Set<String> notSolvedSet = new LinkedHashSet<>();
    Map<String,Integer> notSolvedMap = new LinkedHashMap();
    Map<String, String> solvedSteps = new LinkedHashMap<>();
    public static final int width = 68;
    public static final int height = 68;
    private int offset = 0;
    public static int columnNum = 12;
    public static int rowNum = 8;
    static int[][] gameMatrix = new int[rowNum][columnNum];
    static final int SOLVED = -1;
    static final int NOT_FOUND = -2;

    public Mat[][] splitImageAndSave(String imageFile) {
        long start = System.currentTimeMillis();
        //Instantiating the Imgcodecs class
        //Reading the Image from the file
        Mat img = Imgcodecs.imread(imageFile);
        // HighGui.imshow("hihi", img);
        // HighGui.waitKey(0);
        System.out.println(img.size());
        int w = img.width();
        int h = img.height();

        Mat[][] result = new Mat[rowNum][columnNum];
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < columnNum; j++) {
                int x = j * width;
                int y = i * height;

                if (x + width > w) {
                    System.out.println("width is over with i:" + i + ", width: " + x);
                }
                if (y + height > h) {
                    System.out.println("height is over with j:" + j + ", height:" + (y));
                }
                Mat copyImg = img.clone();
                //System.out.println(copyImg.get(0,0));

                //img.copyTo(copyImg);
                Rect rectCrop = new Rect(x +offset, y + offset, width - offset - 1, height - offset - 1);
                Mat cropImg = new Mat(copyImg, rectCrop);


                // Mat cropImg = img.submat(rectCrop);
                String fileName = "images/crop/" + (i + 1) + "_" + (j + 1) + ".jpg";
                Imgcodecs.imwrite(fileName, cropImg);
                result[i][j] = cropImg;
                copyImg.release();

            }
        }

        System.out.println("image splitter takes: " +(System.currentTimeMillis() - start));
        return result;
    }

    public int[][] processImage(Mat[][] images, boolean isFirstProcess) {
        long start = System.currentTimeMillis();
        int[][] result = new int[rowNum][columnNum];


        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < columnNum; j++) {
                Mat mat = images[i][j];
                if (i == 2 && j == 11) {
                    // HighGui.imshow("source", mat);
                    // HighGui.imshow("template", imageMap.get(11));
                    //HighGui.waitKey(0);
                }
                int index = findExistImageInMap(mat, isFirstProcess);

                //System.out.println("i, j = " + i + "," + j + ", key=" + index);

                result[i][j] = index;
                String cellString = i+ "_" + j;
                //notSolvedSet.add(cellString);
                notSolvedMap.put(cellString, index);
            }

        }
        gameMatrix = result;
        System.out.println("image processing takes:" + (System.currentTimeMillis() - start));
        return result;
    }

    public int findExistImageInMap(Mat mat, boolean isFirstProcess) {
        boolean found = false;
        for (Integer key : imageMap.keySet()) {
            Mat template = imageMap.get(key);
            //process template maching


            //template matching
            Mat result = new Mat();

            Mat grayMat = new Mat();
            Mat grayTemplate = new Mat();
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY);
            //System.out.println(grayMat.size());
            Rect rectCrop = new Rect(10, 10, width - 20, height - 20);
            Mat cropGrayMat = new Mat(grayMat, rectCrop);

            Imgproc.cvtColor(template, grayTemplate, Imgproc.COLOR_BGR2GRAY);
            Imgproc.matchTemplate(cropGrayMat, grayTemplate, result, Imgproc.TM_CCOEFF_NORMED);


            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            Point matchLoc = mmr.maxLoc;
            //Draw rectangle on result image
            //System.out.println("maxVal: " + mmr.maxVal);
            if (mmr.maxVal > 0.5) {
                //System.out.println("found at key: " + key);
                return key;
            }

        }
        if(isFirstProcess) {
            int newElementNum = imageMap.size();
            imageMap.put(newElementNum, mat);
            return newElementNum;
        }
        return NOT_FOUND;
    }

    public  String findImageIndex(String source){
        for (String cell: notSolvedMap.keySet()){
            if(cell.equals(source)){
                continue;
            }

            if(isImageMatched(source, cell)){
                return cell;
            }
        }
        return null;
    }
    public  boolean isImageMatched(String source, String target) {
        int sourceRowIndex = Integer.parseInt(source.split("_")[0]);
        int sourceColumnIndex = Integer.parseInt(source.split("_")[1]);

        int targetRowIndex = Integer.parseInt(target.split("_")[0]);
        int targetColumnIndex = Integer.parseInt(target.split("_")[1]);

        int sourceValue = gameMatrix[sourceRowIndex][sourceColumnIndex];
        int targetValue = gameMatrix[targetRowIndex][targetColumnIndex];

        if(sourceValue != targetValue) {
            return false;
        }
        //check left rows both source and target
        boolean ok = checkLeftRowsEmpty(sourceRowIndex,sourceColumnIndex);

        if(ok) {
            ok = checkLeftRowsEmpty(targetRowIndex,targetColumnIndex);
        }
        if(ok){
            return true;
        }
        //check right rows both source and target
        ok = checkRightRowsEmpty(sourceRowIndex,sourceColumnIndex);

        if(ok) {
            ok = checkRightRowsEmpty(targetRowIndex,targetColumnIndex);
        }
        if(ok){
            return true;
        }

        //check right rows both source and target
        ok = checkTopColsEmpty(sourceRowIndex,sourceColumnIndex);

        if(ok) {
            ok = checkTopColsEmpty(targetRowIndex,targetColumnIndex);
        }
        if(ok){
            return true;
        }

        //check right rows both source and target
        ok = checkBottomColsEmpty(sourceRowIndex,sourceColumnIndex);

        if(ok) {
            ok = checkBottomColsEmpty(targetRowIndex,targetColumnIndex);
        }
        if(ok){
            return true;
        }

        return false;

    }

    public  boolean checkLeftRowsEmpty(int rowIndex, int colIndex){
        for (int i = 0; i < colIndex; i++) {
            int value = gameMatrix[rowIndex][i];
            if(value != SOLVED) {
                return false;
            }
        }
        return true;
    }

    public  boolean checkRightRowsEmpty(int rowIndex, int colIndex){
        for (int i = colIndex + 1; i < columnNum; i++) {
            int value = gameMatrix[rowIndex][i];
            if(value != SOLVED) {
                return false;
            }
        }
        return true;
    }

    public boolean checkTopColsEmpty(int rowIndex, int colIndex){
        for (int i = 0; i < rowIndex; i++) {
            int value = gameMatrix[i][colIndex];
            if(value != SOLVED) {
                return false;
            }
        }
        return true;
    }
    public boolean checkBottomColsEmpty(int rowIndex, int colIndex){
        for (int i = rowIndex + 1; i < rowNum; i++) {
            int value = gameMatrix[i][colIndex];
            if(value != SOLVED) {
                return false;
            }
        }
        return true;
    }

   /* public static int findHorizontal(int rowIndex, int columnIndex) {
        int value = gameMatrix[rowIndex][columnIndex];
        //if already solved
        if (value == SOLVED) {
            return SOLVED;
        }
        //scan horizontal and all row top to bottom
        //if cell been solved, get drop-down cell until get bottom
        for (int i = columnIndex; i < gameMatrix[0].length; i++) {  //scan all row
            int compareRow = rowIndex;
            int cellValue = gameMatrix[rowIndex][i];

            while (cellValue == SOLVED && compareRow < rowNum) { //reach to most unsolved cell
                compareRow = compareRow + 1;
                cellValue = gameMatrix[compareRow][i];

            }

            if (value == cellValue) {
                gameMatrix[compareRow][columnIndex] = SOLVED;
                gameMatrix[compareRow][i] = SOLVED;
                System.out.println("Found column index at:" +  compareRow + ", " + i);
                return i;
            }
        }
        return NOT_FOUND;
    }

    public static int findVertical(int rowIndex, int columnIndex) {
        int value = gameMatrix[rowIndex][columnIndex];
        //if already solved
        if (value == SOLVED) {
            return SOLVED;
        }
        //scan vertically and all row top to bottom
        //if cell been solved, get drop-down cell until get bottom
        for (int i = rowIndex + 1; i < gameMatrix.length; i++) {  //scan all row
            int compareColumn = columnIndex;
            int cellValue = gameMatrix[i][columnIndex];

            while (cellValue == SOLVED && compareColumn < columnNum) { //reach to most unsolved cell
                compareColumn = compareColumn + 1;
                cellValue = gameMatrix[i][compareColumn];

            }

            if (value == cellValue) {
                gameMatrix[compareColumn][columnIndex] = SOLVED;
                gameMatrix[i][compareColumn]= SOLVED;
                System.out.println("Found row index at:" + i + "," + compareColumn);
                return i;
            }
        }
        return NOT_FOUND;
    }
*/
    public Map<String, String> playGame() {
        //find the position outer.
        //scan horizontally
        long start = System.currentTimeMillis();

        System.out.println("notSolvedSet size:" + notSolvedMap.size());
        int previousSize = Integer.MAX_VALUE;
        while(previousSize != notSolvedMap.size()){
            previousSize = notSolvedMap.size();
            Set<String> set = new LinkedHashSet<>(notSolvedMap.keySet());
            for (String cell: set){
                if(!notSolvedMap.containsKey(cell)){
                    continue;
                }
                String found = findImageIndex(cell);
                if(found != null){
                    solve(cell);
                    solve(found);
                    notSolvedMap.remove(cell);
                    notSolvedMap.remove(found);
                    solvedSteps.put(cell,found);
                }
           /* int rowIndex = Integer.parseInt(cell.split("_")[0]);
            int columnIndex = Integer.parseInt(cell.split("_")[1]);

            int foundIndex = findHorizontal(rowIndex,0);
            if(foundIndex == NOT_FOUND) {
                foundIndex = findVertical(0, columnIndex);
            }
            if(foundIndex > 0){
                System.out.println("row, column found: " + rowIndex + ", " + columnIndex);
                printMatrix();
            }
            */

                // Thread.sleep(2000);
                //System.exit(1);

            }

           // printMatrix();
        }
        System.out.println("step found:" + solvedSteps.size());
        System.out.println("play game takes: " + (System.currentTimeMillis() - start));

        return solvedSteps;

    }

    public boolean isDone(){
        if(notSolvedMap.isEmpty()){
            return true;
        }
        return false;
    }
    public void solve(String cell){
        int rowIndex = Integer.parseInt(cell.split("_")[0]);
        int columnIndex = Integer.parseInt(cell.split("_")[1]);
        gameMatrix[rowIndex][columnIndex] = SOLVED;
    }

    public void printMatrix() {
        System.out.println("-----------------------------");
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < columnNum; j++) {
                System.out.print(gameMatrix[i][j] + ", ");
            }
            System.out.println();

        }
    }

    public static void main(String[] args) {
        String gameScreenImage = "E:/git/taint/pikachu/screenshot.jpg";

        ImageProcessing splitter = new ImageProcessing();
        Mat[][] splitImages = splitter.splitImageAndSave(gameScreenImage);
        int[][] matrix = splitter.processImage(splitImages, true);
        splitter.printMatrix();
        splitter.playGame();

    }
}