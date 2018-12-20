package com.wkk.cameralib;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;

    @BindView(R.id.btn_switch)
    Button btnSwitch;

    @BindView(R.id.btn_take)
    Button btnTakePhoto;

    private Unbinder unbinder;
    private CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        applyCameraPermission();
    }

    private void applyCameraPermission() {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if(granted){
                            openCamera();
                        }else {
                            Toast.makeText(MainActivity.this, "打开权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openCamera() {
        cameraHelper = new CameraHelper(this,surfaceView);
    }

    @OnClick({R.id.btn_take,R.id.btn_switch})
    protected void dealOnClick(View view){
        switch (view.getId()){
            case R.id.btn_switch:
                cameraHelper.exchangeCamera();
                break;
            case R.id.btn_take:

                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != unbinder){
            unbinder.unbind();
        }
    }
}
