package net.cattaka.android.learncamera2;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import net.cattaka.android.learncamera2.databinding.ActivityMainBinding;
import net.cattaka.android.learncamera2.utils.CameraEngine2;
import net.cattaka.android.learncamera2.utils.ICameraEngine;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;
    ICameraEngine mCameraEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mCameraEngine = new CameraEngine2(cameraManager, CameraMetadata.LENS_FACING_BACK);
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
    }
}
