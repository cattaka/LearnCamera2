package net.cattaka.android.learncamera2.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;

/**
 * Created by takao on 2016/11/09.
 */

public class CameraEngine2 implements ICameraEngine {

    static Handler sHandler = new Handler(Looper.getMainLooper());

    CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            goNext();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {

        }
    };

    CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            mCameraCaptureSession = cameraCaptureSession;
            goNext();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

        }
    };

    CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            mCaptureRequest = request;
        }
    };

    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            goNext();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireLatestImage();
//            Log.d("test", image.getWidth() + "x" + image.getHeight());
            image.close();
        }
    };

    CameraManager mCameraManager;
    TextureView mTextureView;
    int mFacing;

    boolean mRunning;
    ImageReader mImageReader;
    Size mPreviewSize;

    CameraDevice mCameraDevice;
    CameraCaptureSession mCameraCaptureSession;
    CaptureRequest mCaptureRequest;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraEngine2(CameraManager cameraManager, int facing) {
        mCameraManager = cameraManager;
        mFacing = facing;
    }

    private void goNext() {
        if (mTextureView == null || !mTextureView.isAvailable()) {
            return;
        }
        if (mCameraDevice == null) {
            try {
                String cameraId = findCameraId(mCameraManager, mFacing);

                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap scMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

//                int[] formats = scMap.getOutputFormats();
//                for (int format : formats) {
//                    Log.d("format", "format=" + format);
//                }
                mImageReader = findMaxSizeImageReader(scMap, ImageFormat.YUV_420_888);
                mPreviewSize = findBestPreviewSize(scMap, mImageReader);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, sHandler);

                mCameraManager.openCamera(cameraId, mStateCallback, sHandler);
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if (mCameraCaptureSession == null) {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            List<Surface> surfaces = Arrays.asList(surface, mImageReader.getSurface());
            try {
                mCameraDevice.createCaptureSession(surfaces, mSessionStateCallback, sHandler);
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if (mCaptureRequest == null) {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            try {
                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                builder.addTarget(mImageReader.getSurface());
                builder.addTarget(surface);
                mCameraCaptureSession.setRepeatingRequest(builder.build(), mCaptureCallback, sHandler);
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
            return;
        }
    }

    public static String findCameraId(CameraManager cameraManager, int facing) throws CameraAccessException {
        for (String cameraId : cameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer t = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (t != null && t == facing) {
                return cameraId;
            }
        }
        return null;
    }

    public static ImageReader findMaxSizeImageReader(StreamConfigurationMap map, int imageFormat) throws CameraAccessException {
        List<Size> size = new ArrayList<>(Arrays.asList(map.getOutputSizes(imageFormat)));
        Collections.sort(size, new Comparator<Size>() {
            @Override
            public int compare(Size s1, Size s2) {
                return s2.getWidth() * s2.getHeight() - s1.getWidth() * s1.getHeight();
            }
        });
        Size maxSize = size.get(0);
        ImageReader imageReader = ImageReader.newInstance(maxSize.getHeight(), maxSize.getWidth(), imageFormat, 1);
        return imageReader;
    }

    public static Size findBestPreviewSize(StreamConfigurationMap map, ImageReader imageSize) throws CameraAccessException {
        List<Size> size = new ArrayList<>(Arrays.asList(map.getOutputSizes(SurfaceTexture.class)));
        Collections.sort(size, new Comparator<Size>() {
            @Override
            public int compare(Size s1, Size s2) {
                return s2.getWidth() * s2.getHeight() - s1.getWidth() * s1.getHeight();
            }
        });
        return size.get(0);
    }

    @Override
    public void start() {
        mRunning = true;
        goNext();
    }

    @Override
    public void release() {
        mRunning = false;
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void autoFocus() {
        // no-op
    }

    @Override
    public void setTextureView(TextureView textureView) {
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(null);
        }
        mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        if (mRunning) {
            release();
        }
        goNext();
    }

    @Override
    public void setCameraRotation(int cameraRotation) {
        // TODO
    }

    @Override
    public Observable<Bitmap> createSubject() {
        return Observable.empty();
    }
}
