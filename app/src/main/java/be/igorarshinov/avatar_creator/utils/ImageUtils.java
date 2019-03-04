package be.igorarshinov.avatar_creator.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

@SuppressWarnings("DefaultFileTemplate")
public class ImageUtils {
    private final static int MAXIMUM_IMAGE_SIZE = 1200;
    private static final String LOGTAG = "AvatarAdapter";

    public static Bitmap resizeBitmap(Bitmap bm) {

        int h = bm.getHeight();
        int w = bm.getWidth();

        if (h == w) {
            h = MAXIMUM_IMAGE_SIZE;
            w = MAXIMUM_IMAGE_SIZE;
        } else if (h > w) {
            float ratio = (float) w / (float) h;
            h = MAXIMUM_IMAGE_SIZE;
            w = (int) (MAXIMUM_IMAGE_SIZE * ratio);
        } else {
            float ratio = (float) h / w;
            w = MAXIMUM_IMAGE_SIZE;
            h = (int) (MAXIMUM_IMAGE_SIZE * ratio);
        }

        return Bitmap.createScaledBitmap(bm, w, h, true);
    }

    public static Bitmap makeBitmap(String path) {
        Bitmap bm1 = BitmapFactory.decodeFile(path);
        return resizeBitmap(bm1);
    }

    public static Bitmap[] makeEqualProps(Bitmap bitmap1, Bitmap bitmap2) {

        int maxW = bitmap1.getWidth() > bitmap2.getWidth() ? bitmap1.getWidth() : bitmap2.getWidth();
        int maxH = bitmap1.getHeight() > bitmap2.getHeight() ? bitmap1.getHeight() : bitmap2.getHeight();

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp1Temp = Bitmap.createBitmap(maxW, maxH, conf);
        Bitmap bmp2Temp = Bitmap.createBitmap(maxW, maxH, conf);

        Bitmap[] bmps = new Bitmap[2];
        bmps[0] = ImageUtils.overlay(bmp1Temp, bitmap1);
        bmps[1] = ImageUtils.overlay(bmp2Temp, bitmap2);

        return bmps;
    }

    private static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);

        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap rotatedExifBitmap(String path, Bitmap bitmap) {
        Log.d("ImageUtils", "Load EXIF");
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {

            Log.e(LOGTAG, e.getMessage());
            return bitmap;
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = ImageUtils.rotateBitmap(bitmap, (float) 90.0);
                Log.d("ImageUtils", "Rotated 90 degrees");
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = ImageUtils.rotateBitmap(bitmap, (float) 180.0);
                Log.d("ImageUtils", "Rotated 180 degrees");
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = ImageUtils.rotateBitmap(bitmap, (float) 270.0);
                Log.d("ImageUtils", "Rotated 270 degrees");
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                Log.d("ImageUtils", "Rotated 0 degrees");
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;
    }
}
