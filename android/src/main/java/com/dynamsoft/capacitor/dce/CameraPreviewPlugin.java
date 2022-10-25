package com.dynamsoft.capacitor.dce;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dynamsoft.core.CoreException;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCEPhotoListener;
import com.dynamsoft.dce.EnumCameraState;
import com.dynamsoft.dce.EnumResolution;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.io.ByteArrayOutputStream;

@CapacitorPlugin(
    name = "CameraPreview",
    permissions = {
        @Permission(strings = { Manifest.permission.CAMERA }, alias = CameraPreviewPlugin.CAMERA),
    }
)
public class CameraPreviewPlugin extends Plugin {
    // Permission alias constants
    static final String CAMERA = "camera";
    private CameraEnhancer mCameraEnhancer;
    private DCECameraView mCameraView;
    private EnumCameraState previousCameraStatus;
    private String callbackID;

    @PluginMethod
    public void initialize(PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mCameraEnhancer = new CameraEnhancer(getActivity());
                mCameraView = new DCECameraView(getActivity());
                mCameraEnhancer.setCameraView(mCameraView);
                FrameLayout.LayoutParams cameraPreviewParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                ((ViewGroup) bridge.getWebView().getParent()).addView(mCameraView,cameraPreviewParams);
                bridge.getWebView().bringToFront();
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void startCamera(PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mCameraView.setVisibility(View.VISIBLE);
                    mCameraEnhancer.open();
                    makeWebViewTransparent();
                    triggerOnPlayed();
                    call.resolve();
                } catch (CameraEnhancerException e) {
                    e.printStackTrace();
                    call.reject(e.getMessage());
                }
            }
        });
    }

    @PluginMethod
    public void stopCamera(PluginCall call) {
        try{
            restoreWebViewBackground();
            mCameraView.setVisibility(View.INVISIBLE);
            mCameraEnhancer.close();
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void pauseScan(PluginCall call) {
        try{
            mCameraEnhancer.pause();
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void resumeScan(PluginCall call) {
        try{
            mCameraEnhancer.resume();
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }


    private void makeWebViewTransparent(){
        bridge.getWebView().setTag(bridge.getWebView().getBackground());
        bridge.getWebView().setBackgroundColor(Color.TRANSPARENT);
    }

    private void restoreWebViewBackground(){
        bridge.getWebView().setBackground((Drawable) bridge.getWebView().getTag());
    }

    @PluginMethod
    public void toggleTorch(PluginCall call) {
        try{
            if (call.getBoolean("on",true)){
                mCameraEnhancer.turnOnTorch();
            }else {
                mCameraEnhancer.turnOffTorch();
            }
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void setScanRegion(PluginCall call){
        if (mCameraEnhancer!=null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        JSObject region = call.getObject("region");
                        com.dynamsoft.core.RegionDefinition scanRegion = new com.dynamsoft.core.RegionDefinition();
                        scanRegion.regionTop = region.getInt("top");
                        scanRegion.regionBottom = region.getInt("bottom");
                        scanRegion.regionLeft = region.getInt("left");
                        scanRegion.regionRight = region.getInt("right");
                        scanRegion.regionMeasuredByPercentage = region.getInt("measuredByPercentage");

                        mCameraEnhancer.setScanRegion(scanRegion);
                        call.resolve();
                    } catch (Exception e) {
                        e.printStackTrace();
                        call.reject(e.getMessage());
                    }
                }
            });
        }else{
            call.reject("DCE not initialized");
        }
    }

    @PluginMethod
    public void setZoom(PluginCall call){
        if (call.hasOption("factor")) {
            Float factor = call.getFloat("factor");
            try {
                mCameraEnhancer.setZoom(factor);
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void setFocus(PluginCall call){
        if (call.hasOption("x") && call.hasOption("y")) {
            Float x = call.getFloat("x");
            Float y = call.getFloat("y");
            try {
                mCameraEnhancer.setFocus(x,y);
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void selectCamera(PluginCall call){
        if (call.hasOption("cameraID")){
            try {
                Runnable selectCameraRunnable = new Runnable() {
                    public void run() {
                        try {
                            mCameraEnhancer.selectCamera(call.getString("cameraID"));
                        } catch (CameraEnhancerException e) {
                            e.printStackTrace();
                        }
                        synchronized(this)
                        {
                            this.notify();
                        }
                    }
                };
                synchronized( selectCameraRunnable ) {
                    getActivity().runOnUiThread(selectCameraRunnable);
                    selectCameraRunnable.wait();
                }

                triggerOnPlayed();
            } catch (InterruptedException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        JSObject result = new JSObject();
        result.put("success",true);
        call.resolve(result);
    }

    private void triggerOnPlayed(){
        Size res = mCameraEnhancer.getResolution();
        if (res != null) {
            JSObject onPlayedResult = new JSObject();
            onPlayedResult.put("resolution",res.getWidth() + "x" + res.getHeight());
            Log.d("DBR","resolution:" + res.getWidth() + "x" + res.getHeight());
            notifyListeners("onPlayed",onPlayedResult);
        }
    }

    @PluginMethod
    public void getAllCameras(PluginCall call){
        if (mCameraEnhancer == null) {
            call.reject("not initialized");
        }else {
            JSObject result = new JSObject();
            JSArray cameras = new JSArray();
            for (String camera: mCameraEnhancer.getAllCameras()) {
                cameras.put(camera);
            }
            result.put("cameras",cameras);
            call.resolve(result);
        }
    }

    @PluginMethod
    public void getSelectedCamera(PluginCall call){
        if (mCameraEnhancer == null) {
            call.reject("not initialized");
        }else{
            JSObject result = new JSObject();
            result.put("selectedCamera",mCameraEnhancer.getSelectedCamera());
            call.resolve(result);
        }
    }

    @PluginMethod
    public void setResolution(PluginCall call){
        if (call.hasOption("resolution")){
            try {
                Runnable setResolutionRunnable = new Runnable() {
                    public void run() {
                        try {
                            mCameraEnhancer.setResolution(EnumResolution.fromValue(call.getInt("resolution")));
                        } catch (CameraEnhancerException e) {
                            e.printStackTrace();
                        }
                        synchronized(this)
                        {
                            this.notify();
                        }
                    }
                };
                synchronized( setResolutionRunnable ) {
                    getActivity().runOnUiThread(setResolutionRunnable);
                    setResolutionRunnable.wait();
                }
                triggerOnPlayed();
            } catch (InterruptedException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        JSObject result = new JSObject();
        result.put("success",true);
        call.resolve(result);
    }

    @PluginMethod
    public void getResolution(PluginCall call){
        if (mCameraEnhancer == null) {
            call.reject("DCE not initialized");
        }else{
            String res = mCameraEnhancer.getResolution().getWidth()+"x"+mCameraEnhancer.getResolution().getHeight();
            JSObject result = new JSObject();
            result.put("resolution",res);
            call.resolve(result);
        }
    }

    @PluginMethod
    public void takeSnapshot(PluginCall call){
        try {
            if (mCameraEnhancer.getCameraState() == EnumCameraState.OPENED) {
                Bitmap bitmap = mCameraEnhancer.getFrameFromBuffer(true).toBitmap();
                String base64 = bitmap2Base64(bitmap);
                JSObject result = new JSObject();
                result.put("base64",base64);
                call.resolve(result);
            }else{
                call.reject("camera is not open");
            }
        } catch (Exception e) {
            e.printStackTrace();
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void takePhoto(PluginCall call){
        mCameraEnhancer.takePhoto(new DCEPhotoListener() {
            @Override
            public void photoOutputCallback(byte[] bytes) {
                String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                JSObject result = new JSObject();
                result.put("base64",base64);
                call.resolve(result);
            }
        });
    }

    public static String bitmap2Base64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    @PluginMethod
    public void isOpen(PluginCall call){
        if (mCameraEnhancer != null) {
            JSObject result = new JSObject();
            if (mCameraEnhancer.getCameraState() == EnumCameraState.OPENED) {
                result.put("isOpen",true);
            }else{
                result.put("isOpen",false);
            }
            call.resolve(result);
        }else {
            call.reject("DCE not initialized.");
        }
    }

    @Override
    protected void handleOnPause() {
        if (mCameraEnhancer!=null){
            try {
                previousCameraStatus = mCameraEnhancer.getCameraState();
                mCameraEnhancer.close();
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
        super.handleOnPause();
    }

    @Override
    protected void handleOnResume() {
        if (mCameraEnhancer!=null){
            try {
                if (previousCameraStatus == EnumCameraState.OPENED) {
                    mCameraEnhancer.open();
                }
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
        super.handleOnResume();
    }

    @PluginMethod
    public void requestCameraPermission(PluginCall call) {
        boolean hasCameraPerms = getPermissionState(CAMERA) == PermissionState.GRANTED;
        if (hasCameraPerms == false) {
            Log.d("DBR","no camera permission. request permission.");
            String[] aliases = new String[] { CAMERA };
            requestPermissionForAliases(aliases, call, "cameraPermissionsCallback");
        }else{
            call.resolve();
        }
    }

    @PermissionCallback
    private void cameraPermissionsCallback(PluginCall call) {
        boolean hasCameraPerms = getPermissionState(CAMERA) == PermissionState.GRANTED;
        if (hasCameraPerms) {
            call.resolve();
        }else {
            call.reject("Permission not granted.");
        }
    }
}
