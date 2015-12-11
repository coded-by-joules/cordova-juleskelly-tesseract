package com.juleskelly.tesseract;

// android imports
import android.os.Bundle;
import android.os.Environment;
import android.content.res.AssetManager;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.content.Context;

// java imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.lang.*;

// cordova imports
import org.apache.cordova.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

// TessBaseAPI import for tesseract
import com.googlecode.tesseract.android.TessBaseAPI;

public class TesseractOCR extends CordovaPlugin {
  public final String ACTION_LOADENGINE = "loadEngine";
  public final String ACTION_RECOGNIZEIMAGE = "recognizeImage";
  public final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/OCRFolder/";
  public final String TAG = "OCREngine";
  public final String lang = "eng";

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (ACTION_LOADENGINE.equals(action)) {
      loadEngine(callbackContext);
      return true;
    }
    else if (ACTION_RECOGNIZEIMAGE.equals(action)) {
      JSONObject arg_obj = args.getJSONObject(0);
      String imagePath = arg_obj.getString("imageURL");

      recognizeImage(imagePath, callbackContext);
    };


    return false;
  }

  // load training data for tesseract
  public void loadEngine(final CallbackContext callbackContext) {
    String[] paths = new String[] {DATA_PATH, DATA_PATH + "tessdata/"};

    // create directories if they did not exist
    for (String path : paths) {
      File dir = new File(path);

      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          Log.v(TAG, "Error: Creation of directory " + path + " on sdcard failed");
          callbackContext.error("Error: Creation of directory " + path + " on sdcard failed");
          return;
        }
        else {
          Log.v(TAG, "Directory " + path + " created on sdcard.");
        }
      }
      else {
        Log.v(TAG, "Directory already exists");
      }
    }

    // copy trained data if file doesn't exist
    if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
      try {
        AssetManager assetManager = cordova.getActivity().getApplicationContext().getAssets();
        InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
        OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/" + lang + ".traineddata");

        byte[] buf = new byte[1024]; // 1024 bytes = 1 KB
        int len;

        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        in.close();
        out.close();

        Log.v(TAG, "Copied " + lang + ".traineddata successfully");
      }
      catch (IOException e) {
        Log.e(TAG, "Unable to copy " + lang + ".traineddata: " + e.toString());
        callbackContext.error("Unable to copy " + lang + ".traineddata: " + e.toString());
        return;
      }
    }
    else
      Log.v(TAG, "Train data file already exists");

    Log.v(TAG, "Tesseract engine has been loaded");
    callbackContext.success("Tesseract engine has been loaded");
  }

  // recognize the image using tesseract
  public void recognizeImage(String imageURL, final CallbackContext callbackContext) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 4;

    imageURL = imageURL.substring(7);
    Log.v(TAG, imageURL);
    Log.v(TAG, "Starting image decoding...");
    Bitmap bitmap = BitmapFactory.decodeFile(imageURL, options);

    try {
      // convert captured image to bitmap format
      ExifInterface exif = new ExifInterface(imageURL);
      int exifOrientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
      );

      Log.v(TAG, "Orient: "+ exifOrientation);
      int rotate = 0;
      switch (exifOrientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;
      }
      Log.v(TAG, "Rotation: " + rotate);

      if (rotate != 0) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.preRotate(rotate);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
      }

      bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

      // scan and recognize the bitmap image
      String recognizedText = "";

      Log.v(TAG, "Before baseApi");
      TessBaseAPI baseApi = new TessBaseAPI();
      baseApi.setDebug(true);
      baseApi.init(DATA_PATH, lang);
      baseApi.setImage(bitmap);

      recognizedText = baseApi.getUTF8Text();
      baseApi.end();

      Log.v(TAG, "OCRED Text: " + recognizedText);
			if (lang.equalsIgnoreCase("eng")) {
				recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
			}

			recognizedText = recognizedText.trim();
			Log.v(TAG, "Recognized Text: " + recognizedText);

      callbackContext.success(recognizedText);
      Log.v(TAG, "Scanning completed");
      return;
    }
    catch (Exception ex) {
      Log.e(TAG,ex.getMessage());
      callbackContext.error(ex.getMessage());
      return;
    }
  }
}
