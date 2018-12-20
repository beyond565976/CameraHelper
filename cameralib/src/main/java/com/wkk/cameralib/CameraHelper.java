package com.wkk.cameralib;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PipedReader;
import java.util.List;

/**
 * Created by wangkeke on 2018/12/19.
 */

public class CameraHelper implements Camera.PreviewCallback{

    private Activity activity;

    private SurfaceView surfaceView;

    private Camera mCamera;

    private Camera.Parameters mParameters;

    private SurfaceHolder mSurfaceHolder;

    private SurfaceHolder.Callback mCallBack;

    //摄像头方向
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    //预览旋转的角度
    private int mDisplayOrientation = 0;

    private int picWidth = 2160;
    private int picHeight = 3840;

    public CameraHelper(Activity activity, SurfaceView surfaceView) {
        this.activity = activity;
        this.surfaceView = surfaceView;
        mSurfaceHolder = surfaceView.getHolder();
        init();
    }

    private void init() {
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(mCamera == null){
                    openCamera(mCameraFacing);
                }
                startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }
        });
    }

    private boolean openCamera(int mCameraFacing) {
        boolean supportCameraFacing = supportCameraFacing(mCameraFacing);
        if(supportCameraFacing){
            try {
                mCamera = Camera.open(mCameraFacing);
                initParmeters(mCamera);
                mCamera.setPreviewCallback(this);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(activity, "打开相机失败", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return supportCameraFacing;
    }

    private void initParmeters(Camera mCamera) {
        try {
            mParameters = mCamera.getParameters();
            mParameters.setPictureFormat(ImageFormat.NV21);

            //设置预览尺寸
            Camera.Size bestPreviewSize = getBestSize(surfaceView.getWidth(), surfaceView.getHeight(), mParameters.getSupportedPreviewSizes());
            mParameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

            //设置保存图片尺寸
            Camera.Size bestPicSize = getBestSize(picWidth, picHeight, mParameters.getSupportedPictureSizes());

            mParameters.setPictureSize(bestPicSize.width, bestPicSize.height);

            //对焦模式
            if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            mCamera.setParameters(mParameters);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(activity, "相机初始化失败！", Toast.LENGTH_SHORT).show();
        }

    }

    //判断是否支持某一对焦模式
    private boolean isSupportFocus(String focusMode){
        boolean autoFocus = false;
        List<String> listFocusMode = mParameters.getSupportedFocusModes();
        for (String mode : listFocusMode) {
            if (mode == focusMode)
                autoFocus = true;
            Log.e("wangkeke","相机支持的对焦模式： "+mode);
        }
        return autoFocus;
    }

    private boolean supportCameraFacing(int cameraFacing){
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i,info);
            if(info.facing == cameraFacing){
                return true;
            }
        }
        return false;
    }

    //获取与指定宽高相等或最接近的尺寸
    private Camera.Size getBestSize(int targetWidth, int targetHeight,List<Camera.Size> sizeList){
        Camera.Size bestSize = null;
        float targetRatio = (targetHeight / targetWidth); //目标大小的宽高比
        float minDiff = targetRatio;

        for (Camera.Size size :sizeList) {
            int supportedRatio = (size.width / size.height);
            Log.e("wangkeke","系统支持的尺寸 : "+size.width+"  "+size.height+",    比例 : "+supportedRatio);

        }

        for (Camera.Size size : sizeList) {
            if (size.width == targetHeight && size.height == targetWidth) {
                bestSize = size;
                break;
            }

            float supportedRatio = (size.width / size.height);
            if (Math.abs(supportedRatio - targetRatio) < minDiff) {
                minDiff = Math.abs(supportedRatio - targetRatio);
                bestSize = size;
            }
        }
        Log.e("wangkeke","目标尺寸 ："+targetWidth+" * "+targetHeight+",   比例 : "+targetRatio);
        Log.e("wangkeke","最优尺寸 ："+bestSize.height+" * "+bestSize.width);

        return bestSize;
    }

    //切换摄像头
    public void exchangeCamera() {
        releaseCamera();
        if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK)
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        else
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

        openCamera(mCameraFacing);
        startPreview();
    }

    //释放相机
    private void releaseCamera() {
        if (mCamera != null) {
            // mCamera?.stopFaceDetection()
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    //开始预览
    public void startPreview() {
        try{
            mCamera.setPreviewDisplay(mSurfaceHolder);
            setCameraDisplayOrientation(activity);
            mCamera.startPreview();
//        startFaceDetect();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void startFaceDetect() {

        mCamera.startFaceDetection();
        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {

            }
        });
        /*mCamera.setFaceDetectionListener { faces, _ ->
                    mCallBack?.onFaceDetect(transForm(faces))
                log("检测到 ${faces.size} 张人脸")
            }*/

    }

    //设置预览旋转的角度
    private void setCameraDisplayOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraFacing, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int screenDegree = 0;

        switch (rotation){
            case Surface.ROTATION_0:
                screenDegree = 0;
                break;
            case Surface.ROTATION_90:
                screenDegree = 90;
                break;
            case Surface.ROTATION_180:
                screenDegree = 180;
                break;
            case Surface.ROTATION_270:
                screenDegree = 270;
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mDisplayOrientation = (info.orientation + screenDegree) % 360;
            mDisplayOrientation = (360 - mDisplayOrientation) % 360;     // compensate the mirror
        } else {
            mDisplayOrientation = (info.orientation - screenDegree + 360) % 360;
        }
        mCamera.setDisplayOrientation(mDisplayOrientation);

        Log.e("wangkeke","屏幕的旋转角度 : "+rotation);
        Log.e("wangkeke","setDisplayOrientation(result)  : "+mDisplayOrientation);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }
}
