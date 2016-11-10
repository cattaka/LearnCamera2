package net.cattaka.android.learncamera2.utils;

import android.graphics.Bitmap;
import android.view.TextureView;

import rx.Observer;
import rx.Subscription;

/**
 * Created by takao on 2016/11/09.
 */
public interface ICameraEngine {
    void start();

    void release();

    boolean isRunning();

    void autoFocus();

    void setTextureView(TextureView textureView);

    void setCameraRotation(int cameraRotation);

    Subscription subscribePicture(Observer<Bitmap> observer);

    void takePicture();
}
