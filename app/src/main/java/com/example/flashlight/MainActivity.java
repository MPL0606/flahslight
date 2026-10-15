package com.example.flashlight;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isOn = false;
    private Button toggle;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.rgb(15, 23, 42));
        root.setPadding(32, 32, 32, 32);
        status = new TextView(this);
        status.setTextSize(24);
        status.setTextColor(Color.WHITE);
        status.setGravity(Gravity.CENTER);
        status.setText("Фонарик выключен");
        root.addView(status);
        toggle = new Button(this);
        toggle.setText("ВКЛЮЧИТЬ");
        toggle.setTextSize(22);
        toggle.setTypeface(null, Typeface.BOLD);
        toggle.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 110);
        lp.topMargin = 45;
        root.addView(toggle, lp);
        toggle.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else changeTorch(!isOn);
        });
        setContentView(root);
        refresh();
    }

    private void changeTorch(boolean desired) {
        try {
            if (cameraId == null) {
                for (String id : cameraManager.getCameraIdList()) {
                    CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                    Boolean flash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                    if (Boolean.TRUE.equals(flash) && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id; break;
                    }
                }
            }
            if (cameraId == null) { Toast.makeText(this, "Вспышка не найдена", Toast.LENGTH_LONG).show(); return; }
            cameraManager.setTorchMode(cameraId, desired);
            isOn = desired;
            refresh();
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось переключить фонарик", Toast.LENGTH_LONG).show();
        }
    }
    private void refresh() {
        status.setText(isOn ? "Фонарик включён" : "Фонарик выключен");
        toggle.setText(isOn ? "ВЫКЛЮЧИТЬ" : "ВКЛЮЧИТЬ");
        toggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isOn ? Color.rgb(239, 68, 68) : Color.rgb(250, 204, 21)));
        toggle.setTextColor(Color.BLACK);
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 1 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) changeTorch(!isOn);
        else Toast.makeText(this, "Для фонарика нужно разрешение камеры", Toast.LENGTH_LONG).show();
    }
    @Override protected void onStop() { super.onStop(); if (isOn) changeTorch(false); }
}
