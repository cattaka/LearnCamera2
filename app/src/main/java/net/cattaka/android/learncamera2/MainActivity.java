package net.cattaka.android.learncamera2;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.cattaka.android.learncamera2.databinding.ActivityMainBinding;
import net.cattaka.android.learncamera2.utils.CameraEngine2;
import net.cattaka.android.learncamera2.utils.ICameraEngine;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import rx.Observer;
import rx.Subscription;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;
    ICameraEngine mCameraEngine;
    Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setActivity(this);

        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mCameraEngine = new CameraEngine2(cameraManager, CameraMetadata.LENS_FACING_BACK, 500);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraEngine.setTextureView(mBinding.texture);
        MainActivityPermissionsDispatcher.openCameraWithCheck(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraEngine.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.CAMERA})
    public void openCamera() {
        mCameraEngine.start();

        mSubscription = mCameraEngine.subscribePicture(new Observer<Bitmap>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Bitmap bitmap) {
                mBinding.image.setImageBitmap(bitmap);
            }
        });
    }

    public void onClickImage(View view) {
        mCameraEngine.takePicture();
    }
}
