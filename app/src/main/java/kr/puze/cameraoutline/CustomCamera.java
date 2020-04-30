package kr.puze.cameraoutline;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
@SuppressLint("ViewConstructor")
public class CustomCamera extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private int mCameraID;
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private int mDisplayOrientation;
    private Context cxt;

    public CustomCamera(Context context, int cameraId) {
        super(context);
        cxt = context;
        Log.d("CUSTOMCAMERA", "MyCameraPreview cameraId : " + cameraId);

        mCameraID = cameraId;
        try {
            mCamera = Camera.open(mCameraID);
        } catch (Exception e) {
            Log.e("CUSTOMCAMERA", "Camera is not available");
        }

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mDisplayOrientation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
    }

    //카메라를 생성
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("CUSTOMCAMERA", "surfaceCreated");

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);
        mCameraInfo = cameraInfo;

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("CUSTOMCAMERA", "Error setting camera preview: " + e.getMessage());
        }
    }

    //카메라를 삭제
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("CUSTOMCAMERA", "surfaceDestroyed");

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            holder.removeCallback(this);
        }
    }

    //카메라의 상태 변경
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d("CUSTOMCAMERA", "surfaceChanged");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            Log.e("CUSTOMCAMERA", "preview surface does not exist");
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
            Log.d("CUSTOMCAMERA", "Preview stopped.");
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            Log.d("CUSTOMCAMERA", "Error starting camera preview: " + e.getMessage());
        }


        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
        mCamera.setDisplayOrientation(orientation);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            Log.d("CUSTOMCAMERA", "Camera preview started.");
        } catch (Exception e) {
            Log.d("CUSTOMCAMERA", "Error starting camera preview: " + e.getMessage());
        }

    }

    /**
     * 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다.
     */
    public int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    //이미지 캡처
    public void takePicture() {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    //이미지 저장을 위한 콜백 함수
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {

        }
    };

    private Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };


    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //이미지의 너비와 높이 결정
            int w = camera.getParameters().getPictureSize().width;
            int h = camera.getParameters().getPictureSize().height;
            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
            Intent intent = new Intent(cxt, CameraAddActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra("width", w);
            intent.putExtra("height", h);
            intent.putExtra("orientation", orientation);
            intent.putExtra("camera", mCameraInfo.facing);
            Log.d("CustomCamera", "PictureCallback");
            CameraAddActivity.data = data;
            cxt.startActivity(intent);
        }
    };
}