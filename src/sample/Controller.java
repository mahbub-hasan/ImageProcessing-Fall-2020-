package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.edge_detection.SobelOperator;
import sample.filter.MeanFilter;
import sample.threshold.BasicGlobalThreshold;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ResourceBundle;

public class Controller {

    public ImageView input_image;
    public ImageView output_image;
    public BufferedImage inputBufferedImage, outputImage;
    public MenuBar menuBar;
    private int imageWidth, imageHeight;


    public void openImageFromComputer(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG File","*.jpg"),
                new FileChooser.ExtensionFilter("PNG File","*.png")
        );
        File file = fileChooser.showOpenDialog(null);
        if(file == null) {
            System.out.println("No file choose!!");
            return;
        }
        System.out.println(file.getAbsolutePath());

        // to show the image as input and output
        input_image.setImage(new Image(new FileInputStream(file.getAbsolutePath())));

        // all kinds of calculations
        inputBufferedImage = ImageIO.read(new FileInputStream(file.getAbsolutePath()));

        imageWidth = inputBufferedImage.getWidth();
        imageHeight = inputBufferedImage.getHeight();

    }

    public void saveImageIntoComputer(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG File","*.jpg"),
                new FileChooser.ExtensionFilter("PNG File","*.png")
        );
        File file = fileChooser.showSaveDialog(null);
        if(file==null)
            return;
        ImageIO.write(this.outputImage,"jpg",file);

    }

    public void gotoSingleColorForm(ActionEvent actionEvent) throws IOException {
        if(inputBufferedImage == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setContentText("Input image not found!");
            alert.show();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("singleColor.fxml"));
        Parent parent = loader.load();

        SingleColorFormController controller = (SingleColorFormController) loader.getController();

        Scene scene = new Scene(parent);
        Stage stage = (Stage) menuBar.getScene().getWindow();

        stage.setScene(scene);

        controller.setImportImage(inputBufferedImage);
        controller.setImageIntoImageView();

    }



    public void doConversionFromRBGToCMY(ActionEvent actionEvent) {
        BufferedImage outputBufferedImage =
                new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_3BYTE_BGR);

        for(int row=0;row<4;row++){
            for(int col=0;col<4;col++){
                int rgb = inputBufferedImage.getRGB(col, row);

                //int red = (rgb >> 16) & 0xFF;
                //int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;

                System.out.print(blue+"\t");
            }
            System.out.println();
        }


        for(int row=0;row<imageHeight;row++){
            for(int col=0;col<imageWidth;col++){

                int rgb = inputBufferedImage.getRGB(col, row);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;

                //System.out.println("RGB combine value: "+rgb+", Red->"+red+", Green->"+green+", Blue->"+blue);

                int cyan = 255 - red;
                int magenta = 255 - green;
                int yellow = 255 - blue;

                outputBufferedImage.setRGB(col, row, (cyan<<16)|(magenta<<8)|yellow);
            }
        }

        this.outputImage = outputBufferedImage;

        output_image.setImage(SwingFXUtils.toFXImage(outputBufferedImage,null));

    }

    public void doConversionFromRGBToHSI(ActionEvent actionEvent) {
        BufferedImage outputBufferedImage =
                new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_3BYTE_BGR);

        for(int row=0;row<imageHeight;row++){
            for(int col=0;col<imageWidth;col++){

                int rgb = inputBufferedImage.getRGB(col, row);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;

                float nRed = red / 255.0f;
                float nGreen = green/255.0f;
                float nBlue = blue/255.0f;

                float hue = 0;

                double h1 = (0.5*((nRed-nGreen)+(nRed-nBlue)));
                double h2 = Math.sqrt((Math.pow((nRed-nGreen),2))+((nRed-nBlue)*(nGreen-nBlue)));
                float radianValue = (float) Math.acos(h1/h2);

                float degree = (float) (radianValue*(180/Math.PI));

                if(blue<=green)
                    hue = degree;
                else
                    hue = 360-degree;

                hue = hue/360;


                float saturation=0;
                if((red+green+blue)!=0)
                    saturation = 1-(3/(nRed+nGreen+nBlue))*Math.min(nRed,Math.min(nGreen,nBlue));

                float intensity = (nRed+nGreen+nBlue)/3;

                int h = (int) (hue*255);
                int s = (int) (saturation*255);
                int i = (int) (intensity*255);


                outputBufferedImage.setRGB(col, row, (h<<16)|(s<<8)|i);
            }
        }

        this.outputImage = outputBufferedImage;

        output_image.setImage(SwingFXUtils.toFXImage(outputBufferedImage,null));
    }

    public void doAverageGayScale(ActionEvent actionEvent) {
        BufferedImage outputImage = new
                BufferedImage(inputBufferedImage.getWidth(),inputBufferedImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        for(int row=0;row<inputBufferedImage.getHeight();row++){
            for(int col=0;col<inputBufferedImage.getWidth();col++){
                int rgb = inputBufferedImage.getRGB(col,row);

                int red = (rgb>>16)&0xFF;
                int green = (rgb>>8)&0xFF;
                int blue= (rgb)&0xFF;

                int gray = (red+green+blue)/3;

                outputImage.setRGB(col,row,(gray<<16)|(gray<<8)|gray);
            }
        }

        this.outputImage = outputImage;
        output_image.setImage(SwingFXUtils.toFXImage(outputImage,null));
    }

    public void doDeSaturation(ActionEvent actionEvent) {
        BufferedImage outputImage = new
                BufferedImage(inputBufferedImage.getWidth(),inputBufferedImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        for(int row=0;row<inputBufferedImage.getHeight();row++){
            for(int col=0;col<inputBufferedImage.getWidth();col++){
                int rgb = inputBufferedImage.getRGB(col,row);

                int red = (rgb>>16)&0xFF;
                int green = (rgb>>8)&0xFF;
                int blue= (rgb)&0xFF;

                int max = Math.max(Math.max(red,green),blue);
                int min = Math.min(Math.min(red, green),blue);

                int gray = (max+min)/2;

                outputImage.setRGB(col,row,(gray<<16)|(gray<<8)|gray);
            }
        }
        output_image.setImage(SwingFXUtils.toFXImage(outputImage,null));
    }

    public void calculateGrayScaleHistogram(ActionEvent actionEvent) {
        int[] histogram = new int[256];

        BufferedImage grayScaleImage = getGrayScaleImage();

        WritableRaster raster = grayScaleImage.getRaster();

        for(int row=0; row<grayScaleImage.getHeight(); row++){
            for(int col=0; col<grayScaleImage.getWidth(); col++){

                int pixel = raster.getSample(col, row, 0);

                histogram[pixel]++;

            }
        }

        for(int index=0;index<histogram.length;index++){
            System.out.println("Pixel: "+index+", Count: "+histogram[index]);
        }
    }

    private BufferedImage getGrayScaleImage() {
        BufferedImage outputImage = new
                BufferedImage(inputBufferedImage.getWidth(),inputBufferedImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        for(int row=0;row<inputBufferedImage.getHeight();row++){
            for(int col=0;col<inputBufferedImage.getWidth();col++){
                int rgb = inputBufferedImage.getRGB(col,row);

                int red = (rgb>>16)&0xFF;
                int green = (rgb>>8)&0xFF;
                int blue= (rgb)&0xFF;

                int gray = (red+green+blue)/3;

                outputImage.setRGB(col,row,(gray<<16)|(gray<<8)|gray);
            }
        }
        return outputImage;
    }

    public void doBGT(ActionEvent actionEvent) {
        BasicGlobalThreshold bgt = new BasicGlobalThreshold(getGrayScaleImage());
        BufferedImage bufferedImage = bgt.doBasicGlobalThreshold();
        output_image.setImage(SwingFXUtils.toFXImage(bufferedImage,null));
        this.outputImage = bufferedImage;
    }

    public void doMeanFilter(ActionEvent actionEvent) {
        MeanFilter meanFilter = new MeanFilter(getGrayScaleImage());
        BufferedImage bufferedImage = meanFilter.getMeanFilteringWithBorder();
        output_image.setImage(SwingFXUtils.toFXImage(bufferedImage,null));
        this.outputImage = bufferedImage;
    }

    public void applySobelOperator(ActionEvent actionEvent) {
        SobelOperator sobelOperator = new SobelOperator(inputBufferedImage);
        BufferedImage bufferedImage = sobelOperator.edgeDetectionBySobel();
        output_image.setImage(SwingFXUtils.toFXImage(bufferedImage,null));
        this.outputImage = bufferedImage;
    }
}
