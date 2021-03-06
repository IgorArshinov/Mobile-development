package be.igorarshinov.avatar_creator.createavatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.android.Utils.matToBitmap;

class FaceSwap {
    private static final String LOGTAG = "FaceSwap";
    @SuppressWarnings("JniMissingFunction")
    public native void portraitSwapNative(long addrImg1,
                                          long addrImg2,
                                          int[] landmarksX1,
                                          int[] landmarksY1,
                                          int[] landmarksX2,
                                          int[] landmarksY2,
                                          long addrResult);

    static {

        if (!OpenCVLoader.initDebug()) ;
        else {
            System.loadLibrary("faceswapper");
            System.loadLibrary("opencv_java4");
        }
    }

    enum fsStatus {
        FACE_SWAP_OK,
        FACE_SWAP_NOK,
        FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE1,
        FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE2,
        FACE_NOT_FOUND_IMAGE1,
        FACE_NOT_FOUND_IMAGE2,
        FACE_SWAP_TOO_FEW_FACES
    }

    private Bitmap res;
    private Context context;

    private final static int LANDMARK_SIZE = 15;

    FaceSwap(Context context) {
        this.context = context;
    }

    public Bitmap getRes() {
        return res;
    }

    fsStatus selfieSwap(Bitmap bitmap1, Bitmap bitmap2) {

        ArrayList<ArrayList<PointF>> landmarks1 = getFacialLandmarks(bitmap1);
        ArrayList<ArrayList<PointF>> landmarks2 = getFacialLandmarks(bitmap2);

        if (landmarks1.size() == 0) return fsStatus.FACE_NOT_FOUND_IMAGE1;

        if (landmarks2.size() == 0) return fsStatus.FACE_NOT_FOUND_IMAGE2;
        int faceIdx = 0;
        res = bitmap2.copy(bitmap2.getConfig(), true);
        for (ArrayList<PointF> pList2 : landmarks2) {
            ArrayList<PointF> plist1 = landmarks1.get(faceIdx);

            if (plist1.size() != LANDMARK_SIZE)
                return fsStatus.FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE1;
            if (pList2.size() != LANDMARK_SIZE)
                return fsStatus.FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE2;
            res = swap(bitmap1, res, plist1, pList2);
            faceIdx++;
            if (faceIdx == landmarks1.size()) faceIdx = 0;
        }
        return fsStatus.FACE_SWAP_OK;
    }

    fsStatus multiSwap(Bitmap bitmap) {

        ArrayList<ArrayList<PointF>> landmarks = getFacialLandmarks(bitmap);

        if (landmarks.size() < 2) return fsStatus.FACE_SWAP_TOO_FEW_FACES;
        if (landmarks.size() == 2) {
            if (landmarks.get(0).size() != LANDMARK_SIZE)
                return fsStatus.FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE1;
            if (landmarks.get(1).size() != LANDMARK_SIZE)
                return fsStatus.FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE1;
        }
        Bitmap bitmap1 = bitmap.copy(bitmap.getConfig(), true);
        Bitmap bitmap2 = bitmap.copy(bitmap.getConfig(), true);
        int faceSwapCount = 0;

        int i = 0;
        while (i < landmarks.size() - 1) {
            if (landmarks.get(i).size() != LANDMARK_SIZE) {
                i++;
            } else {
                bitmap2 = swap(bitmap1, bitmap2, landmarks.get(i), landmarks.get(i + 1));
                bitmap2 = swap(bitmap1, bitmap2, landmarks.get(i + 1), landmarks.get(i));
                faceSwapCount++;
                i += 2;
            }
        }

        if (landmarks.size() % 2 == 1) {
            int ind = landmarks.size();
            if (landmarks.get(ind - 2).size() == LANDMARK_SIZE && landmarks.get(ind - 2).size() == LANDMARK_SIZE) {
                bitmap2 = swap(bitmap2, bitmap2, landmarks.get(ind - 2), landmarks.get(ind - 1));
                bitmap2 = swap(bitmap1, bitmap2, landmarks.get(ind - 1), landmarks.get(ind - 2));
                faceSwapCount++;
            }
        }
        if (faceSwapCount == 0) return fsStatus.FACE_SWAP_TOO_FEW_FACES;
        res = bitmap2;
        return fsStatus.FACE_SWAP_OK;
    }

    private Bitmap swap(Bitmap bmp1, Bitmap bmp2, ArrayList<PointF> pts1, ArrayList<PointF> pts2) {

        int[] X1 = new int[pts1.size()];
        int[] Y1 = new int[pts1.size()];
        int[] X2 = new int[pts2.size()];
        int[] Y2 = new int[pts2.size()];
        for (int i = 0; i < pts1.size(); ++i) {
            int x1 = pts1.get(i).X();
            int y1 = pts1.get(i).Y();
            X1[i] = x1;
            Y1[i] = y1;
            int x2 = pts2.get(i).X();
            int y2 = pts2.get(i).Y();
            X2[i] = x2;
            Y2[i] = y2;
        }

        Mat img1 = new Mat();
        bitmapToMat(bmp1, img1);
        Mat img2 = new Mat();
        bitmapToMat(bmp2, img2);

        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGRA2BGR);
        Mat swapped = new Mat();

        portraitSwapNative(img1.getNativeObjAddr(), img2.getNativeObjAddr(), X1, Y1, X2, Y2, swapped.getNativeObjAddr());

        Bitmap bmp = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), Bitmap.Config.ARGB_8888);
        matToBitmap(swapped, bmp);
        return bmp;
    }

    private static final float FACE_CONST = 0.14f;
    private static final float FACE_CONST_MOUTH = 0.11f;
    private static final float FACE_CONST_EYE = 0.19f;

    private ArrayList<ArrayList<PointF>> getFacialLandmarks(Bitmap bitmap) {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);
        ArrayList<Face> faces2 = new ArrayList<>();
        for (int i = 0; i < faces.size(); i++)
            faces2.add(faces.get(faces.keyAt(i)));

        try {
            Collections.sort(faces2, new FaceComparator());
        } catch (NullPointerException e) {
            Log.e(LOGTAG, e.getMessage());
        }
        ArrayList<ArrayList<PointF>> landmarks = new ArrayList<>();
        for (int i = 0; i < faces2.size(); i++) {
            List<Landmark> landmark = faces2.get(i).getLandmarks();
            float faceW = faces2.get(i).getWidth();
            ArrayList<PointF> lm_temp = new ArrayList<>();

            double theta = -1.0 * (faces2.get(i).getEulerZ() * Math.PI / 180.0);
            double theta90 = theta + Math.PI / 2.0;

            float xForeHeadMid = 0.0f;
            float yForeHeadMid = 0.0f;
            float xForeHeadLeft = 0.0f;
            float yForeHeadLeft = 0.0f;
            float xForeHeadRight = 0.0f;
            float yForeHeadRight = 0.0f;
            float xMouthLeft = 0.0f;
            float yMouthLeft = 0.0f;
            float xMouthRight = 0.0f;
            float yMouthRight = 0.0f;
            float xLeftEye = 0.0f;
            float yLeftEye = 0.0f;
            float xRightEye = 0.0f;
            float yRightEye = 0.0f;

            for (Landmark lms : landmark) {
                float x1 = lms.getPosition().x, y1 = lms.getPosition().y;
                switch (lms.getType()) {
                    case Landmark.RIGHT_CHEEK:
                        x1 = (float) (x1 + 0.8 * FACE_CONST * faceW * Math.cos(Math.PI + theta));
                        y1 = (float) (y1 + FACE_CONST * faceW * Math.sin(Math.PI + theta));
                        xRightEye += x1;
                        yRightEye += y1;
                        break;
                    case Landmark.LEFT_CHEEK:
                        x1 = (float) (x1 + 0.8 * FACE_CONST * faceW * Math.cos(theta));
                        y1 = (float) (y1 + FACE_CONST * faceW * Math.sin(theta));
                        xLeftEye += x1;
                        yLeftEye += y1;
                        break;
                    case Landmark.RIGHT_MOUTH:
                        x1 = (float) (x1 + 0.75 * FACE_CONST * faceW * Math.cos((-Math.PI / 8) + Math.PI + theta));
                        y1 = (float) (y1 + 0.75 * FACE_CONST * faceW * Math.sin((-Math.PI / 8) + Math.PI + theta));
                        xMouthRight += x1;
                        yMouthRight += y1;
                        break;
                    case Landmark.LEFT_MOUTH:
                        x1 = (float) (x1 + 0.75 * FACE_CONST * faceW * Math.cos(Math.PI / 8 + theta));
                        y1 = (float) (y1 + 0.75 * FACE_CONST * faceW * Math.sin(Math.PI / 8 + theta));
                        xMouthLeft += x1;
                        yMouthLeft += y1;
                        break;
                    case Landmark.BOTTOM_MOUTH:
                        x1 = (float) (x1 + FACE_CONST_MOUTH * faceW * Math.cos(theta90));
                        y1 = (float) (y1 + FACE_CONST_MOUTH * faceW * Math.sin(theta90));
                        xMouthLeft += x1;
                        xMouthRight += x1;
                        yMouthLeft += y1;
                        yMouthRight += y1;
                        break;
                    case Landmark.RIGHT_EYE:
                        x1 = (float) (x1 + 1.05 * FACE_CONST_EYE * faceW * Math.cos(Math.PI + Math.PI / 5 + theta));
                        y1 = (float) (y1 + FACE_CONST_EYE * faceW * Math.sin(Math.PI + Math.PI / 5 + theta));
                        xRightEye += x1;
                        yRightEye += y1;
                        xForeHeadMid += x1;
                        yForeHeadMid += y1;
                        xForeHeadRight += x1;
                        yForeHeadRight += y1;
                        break;
                    case Landmark.LEFT_EYE:
                        x1 = (float) (x1 + 1.05 * FACE_CONST_EYE * faceW * Math.cos(-Math.PI / 5 + theta));
                        y1 = (float) (y1 + FACE_CONST_EYE * faceW * Math.sin(-Math.PI / 5 + theta));
                        xLeftEye += x1;
                        yLeftEye += y1;
                        xForeHeadMid += x1;
                        yForeHeadMid += y1;
                        xForeHeadLeft += x1;
                        yForeHeadLeft += y1;
                        break;
                    default:
                        break;
                }
                PointF pt = new PointF((int) x1, (int) y1);
                lm_temp.add(pt);
            }

            xForeHeadMid /= 2;
            yForeHeadMid = (float) (yForeHeadMid / 2 - faceW * 0.07);

            yForeHeadLeft += yForeHeadMid;
            xForeHeadLeft += xForeHeadMid;

            yForeHeadRight += yForeHeadMid;
            xForeHeadRight += xForeHeadMid;
            PointF leftDownMouth = new PointF((int) (0.51 * xMouthLeft), (int) (0.51 * yMouthLeft));
            PointF rightDownMoth = new PointF((int) (0.49 * xMouthRight), (int) (0.51 * yMouthRight));
            PointF foreHeadMid = new PointF((int) xForeHeadMid, (int) yForeHeadMid);
            PointF foreHeadLeft = new PointF((int) (0.51 * xForeHeadLeft), (int) (0.48 * yForeHeadLeft));
            PointF foreHeadRight = new PointF((int) (0.49 * xForeHeadRight), (int) (0.48 * yForeHeadRight));
            PointF rightEye = new PointF((int) (0.5 * xRightEye), (int) (0.5 * yRightEye));
            PointF leftEye = new PointF((int) (0.5 * xLeftEye), (int) (0.5 * yLeftEye));
            lm_temp.add(leftDownMouth);
            lm_temp.add(rightDownMoth);
            lm_temp.add(foreHeadMid);
            lm_temp.add(foreHeadLeft);
            lm_temp.add(foreHeadRight);
            lm_temp.add(rightEye);
            lm_temp.add(leftEye);
            landmarks.add(lm_temp);
        }
        detector.release();
        return landmarks;
    }

    private class FaceComparator implements Comparator<Face> {
        @Override
        public int compare(Face lhs, Face rhs) {
            return (int) (lhs.getPosition().x) - (int) (rhs.getPosition().x);
        }
    }

    @SuppressWarnings("unused")
    private Bitmap drawLandmarks(Bitmap bitmap, ArrayList<ArrayList<PointF>> landmarks) {
        Bitmap bitmap3 = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(bitmap3);
        for (int i = 0; i < landmarks.size(); i++) {
            for (int j = 0; j < landmarks.get(i).size(); j++) {
                int cx = landmarks.get(i).get(j).X();
                int cy = landmarks.get(i).get(j).Y();
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setColor(Color.GREEN);
                canvas.drawCircle(cx, cy, 10, paint);
            }
        }
        return bitmap3;
    }
}
