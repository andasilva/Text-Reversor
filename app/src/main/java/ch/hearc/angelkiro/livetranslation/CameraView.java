package ch.hearc.angelkiro.livetranslation;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.util.List;
/**
 * Created by Andre on 07.04.2018.
 */

public class CameraView extends JavaCameraView implements Camera.PictureCallback{

    private static final String TAG = "CameraView";
    private String mPictureFileName;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            fos.write(data);
            fos.close();
            Intent intent = new Intent(getContext(), DisplayResult.class);
            intent.putExtra("filename", mPictureFileName);
            getContext().startActivity(intent);
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
