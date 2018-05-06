package ch.hearc.angelkiro.livetranslation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisplayResult extends Activity {

    private ImageView imageView;
    private Bitmap imageBitmap;
    private Mat imageRGB;
    private Mat imageGrayScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the image taken
        String filename = getIntent().getStringExtra("filename");
        imageBitmap = BitmapFactory.decodeFile(filename);

        // Init Mat
        imageGrayScale = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8UC1);
        imageRGB = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8UC3);

        //Reverse the text on the image
        reverseText();

        //Display the result
        setContentView(R.layout.activity_display_result);
        imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(imageBitmap);
    }

    /**
     * Reverse the text founded in the pictures
     */
    private void reverseText() {
        // Get the image as MAT format
        Utils.bitmapToMat(imageBitmap, imageRGB);

        // Flip the original image
        Core.flip(imageRGB, imageRGB, Core.ROTATE_180);

        // Get a list of characters founded in the image
        List<MatOfPoint> contours = detectCharacters();

        // Flip each character founded
        flipCharacter(contours, false);

        // Convert the result to bitmap
        Utils.matToBitmap(imageRGB, imageBitmap);
    }

    /**
     * Flip each contours in the picture.
     * @param contours Contours of each characters
     * @param debug draw a rectangle around the contours (useful during debugging)
     */
    private void flipCharacter(List<MatOfPoint> contours, boolean debug) {
        for (MatOfPoint contour : contours) {
            //Get the rectangle that fit for each contour
            RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            if (rotatedRect.size.width > 20) {
                if(debug)
                    drawRotatedRect(imageRGB, rotatedRect, new Scalar(255, 0, 0), 4);
                Mat charFounded = new Mat(imageRGB, Imgproc.boundingRect(contour));
                Core.flip(charFounded, charFounded,Core.ROTATE_180);
                imageRGB.copyTo(charFounded);
            }
        }
    }


    /**
     * Detect characters in the picture by thresholding the original image
     * @return A list with the contour of each character
     */
    private List<MatOfPoint> detectCharacters() {
        // Convert the MAT RGB to GRAYSCALE
        Imgproc.cvtColor(imageRGB, imageGrayScale, Imgproc.COLOR_BGR2GRAY);

        // Make a Threshold to detect text
        Imgproc.threshold(imageGrayScale, imageGrayScale, 0, 255, Imgproc.THRESH_OTSU);

        // Invert the image
        Core.bitwise_not(imageGrayScale, imageGrayScale);

        // Get the external contours of characters in the image
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageGrayScale, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours;
    }


    /**
     * Function used to draw the countour of the characters founded in the image (used during debug)
     * @param image image where we want to draw
     * @param rotatedRect the rectangle to draw (position and size)
     * @param color the color of the rectangle
     * @param thickness the thickness of the line drawn
     */
    public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
    }
}
