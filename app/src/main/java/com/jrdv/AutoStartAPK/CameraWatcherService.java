/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jrdv.AutoStartAPK;

import android.app.NotificationManager;
import android.app.PendingIntent;
import java.io.IOException;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import android.opengl.GLES20;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.jwetherell.motion_detection.detection.RgbMotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;


//v0.1 compilado  para android studio ok de github:https://github.com/mienaikoe/DeltaMonitor




public class CameraWatcherService extends Service {

    private static final String TAG = "CameraWatcherService";

    private Camera camera;
    private Camera.Size size;
    private byte[] buffer;
    private SurfaceTexture texture;
    private boolean toastPopped = false;
    private boolean bound = false;
    private static final int NOTIFICATION_ID = 614;
    private RgbMotionDetection detector = null;
    private NotificationManager notifier;

    public Camera getCamera() {
        return camera;
    }

    private static SurfaceTexture getTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
        return new SurfaceTexture(textures[0]);
    }

    @Override
    public void onCreate() {
        detector = new RgbMotionDetection();

        notifyMessage("DeltaMonitor Running","Touch to Preview Camera");
        
        try{
            super.onCreate();
            startRecording();
        } catch(Exception ex){
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "============Destroying CameraWatcherService");
        stopRecording();
        if(camera != null){
            camera.release();
        }
        notifier.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    public void startRecording() {
        if (camera == null) {
            try {
                camera = Camera.open();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
                return;
            }
            if( camera == null ){
                Log.e(TAG, "Camera is null despite trying to allocate it. Stopping Service");
                throw new IllegalStateException("DeltaMonitor was unable to allocate the camera.");
            }
        }

        try {
            Log.i(TAG, "==================Beginning to Record");

            if (buffer == null) {
                Camera.Parameters parameters = CameraSizer.sizeUp(camera);
                size = parameters.getPreviewSize();
                buffer = new byte[size.height * size.width * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
            }
            camera.addCallbackBuffer(buffer);

            if (texture == null) {
                texture = getTexture();
            }
            camera.setPreviewTexture(texture);
            
            detector.reset();
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.startPreview();

        } catch (IOException ex) {
            Log.e(TAG, "IOException during recording setup " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            if( camera != null){
                camera.stopPreview();
                camera.setPreviewCallbackWithBuffer(null);
                camera.setPreviewCallback(null);
                camera.setPreviewTexture(null);
            }
            texture = null; //TODO: This is a patch for a bug (SurfaceTexture has been abandoned)
        } catch (IOException ex) {
            Log.e(TAG, "IOException during recording setdown " + ex.getMessage());
            ex.printStackTrace();
        }

        toastPopped = false;
    }

    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {

            if (data == null) {
                return;
            }
            if (size == null) {
                return;
            }

            int[] img = ImageProcessing.decodeYUV420SPtoRGB(buffer, size.width, size.height);
            if (img != null && detector.detect(img, size.width, size.height)) {
                Log.i(TAG, "======================================= Motion Detected");
                stopRecording();
                Intent intent = new Intent(CameraWatcherService.this, MotionDetectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                camera.addCallbackBuffer(buffer);
                if (!toastPopped) {
                    toastPopped = true;
                    Toast.makeText(getBaseContext(), "DeltaMonitor is now recording in the background. Changes in the camera's view will wake up camera preview.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public class CameraWatcherServiceBinder extends Binder {

        CameraWatcherService getService() {
            return CameraWatcherService.this;
        }
    }

    private final IBinder binder = new CameraWatcherServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        bound = true;
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        bound = false;
        return super.onUnbind(intent);
    }
    
    public void setSensitivity(int sensitivity){
        this.detector.setmThreshold(90-sensitivity);
    }
    
    public int getSensitivity(){
        return 90-this.detector.getmThreshold();
    }
    
    
    
    
    private void notifyMessage(String title, String message) {
        NotificationCompat.Builder mBuilder
                = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(message);
        
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MotionDetectionActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MotionDetectionActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
        mBuilder.setContentIntent(resultPendingIntent);

        if( notifier == null){
            notifier = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        notifier.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
