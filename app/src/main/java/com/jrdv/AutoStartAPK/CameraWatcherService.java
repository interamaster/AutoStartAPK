/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jrdv.AutoStartAPK;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.app.TaskStackBuilder;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

import android.hardware.display.DisplayManager;
import android.opengl.GLES20;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.jwetherell.motion_detection.detection.RgbMotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;


//v0.1 compilado  para android studio ok de github:https://github.com/mienaikoe/DeltaMonitor 3





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


    //para el intnt Extra info

    public static final String  EXTRA_MESSAGE="mensaje";
    private BroadcastReceiver mReceiver;

    private boolean checkhaygente =false;
    private Context mcontext;
    private   Timer timer = new Timer();


    private int numdetecciones=1;

    private   PowerManager.WakeLock wl;
    private PowerManager pm;

    long tiempoAutocheckpeople=20000;//10 min=60*1000*10


    private  SharedPreferences mPrefs;

    //para el device manager

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;


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

          pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
          wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHESS");



        numdetecciones=1;

        Log.i(TAG, "===========================oncreate numdetecciones:"+numdetecciones);


        mcontext=this;

        detector = new RgbMotionDetection();

        notifyMessage("AutoStartAPK Running","Toca para ajustes!!!");
        
        try{
            super.onCreate();


            //startRecording();TODO garbara cuando se apague pantalla



        } catch(Exception ex){
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            onDestroy();
        }


        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        //NO CREO Q SEA NECESARIO LA TENRELO EN MANIFEST!!!NO!!! SI LO QUITO NO FUNCIONA!!

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
          mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);




        //ajustamos sensibilidad

        mPrefs = getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE);
        int valorajuste = mPrefs.getInt("ajustesensibilidad",  50);//"No name defined" is the default value.


        this.detector.setmThreshold(90-valorajuste);

        Log.d("INFO", "sensibilidad ajustada a :"+valorajuste);



        //chequeamos quiettime

        boolean quiettime = mPrefs.getBoolean("quiettime",  true);//"No name defined" is the default value.




        Log.d("INFO", "quiet time habilitado:"+quiettime);


    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////METODO QUE SE EJECUTA CADA VEZ QUE SE RELANZA ESTE SERVICE//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO OJO ESTE METODO SE EJECUTA CADA VEZ QUE SE LANZA UN INTENT DE ESTE SERVICE
        //SI YA ESTABA CREADO!!!
        //ASI QUE ES LA MEJO MANERA DE ACTUALIZAR LA INFO!!

        //ej leer el extra del intent:


        Log.d("INFO", "REINICIADO onStartCommand EN SERVICE!!");






        if (intent == null) {

            //esto solo debe suceder al removeontask o al destroyed el service!!
            //pero ninguno de los 2 metodos que lo deberian detectar o hace...


        }


        if (intent != null) {

            Log.d("INFO", "intent not null  onStartCommand EN SERVICE!!" + intent.getStringExtra(EXTRA_MESSAGE));


            //1º)sacamos los valores de EXTRA_TIME y EXTRA_MSG

            String intentExtra = intent.getStringExtra(EXTRA_MESSAGE);
            Log.v("TASK", "El mensaje recibido en LockService: " + intentExtra);


            //2º)chequeamos si es un intent de pantalla
            if (intentExtra != null && intentExtra.equals("DesdeAjustes")) {


                //ajustamos los valores de timepo y sensibilidad

                //ajustamos sensibilidad

                mPrefs= getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE);
                int valorajuste = mPrefs.getInt("ajustesensibilidad",  50);//"No name defined" is the default value.


                this.detector.setmThreshold(90-valorajuste);

                Log.d("INFO", "sensibilidad ajustada a :"+valorajuste);



                //ajustamos timepo espera


                int timepoespera = mPrefs.getInt("ajustetiempo",  20000);//"No name defined" is the default value.


                tiempoAutocheckpeople=timepoespera*1000*60;



                Log.d("INFO", "timepo espera ajustada a :"+tiempoAutocheckpeople);


            }



             if (isInQuietTime()){

                Log.d("INFO", "quiet time no hago nada!!");


                 return START_STICKY;
             }




            //2º)chequeamos si es un intent de pantalla
            if (intentExtra != null && intentExtra.equals("screen_state")) {


                boolean screenOn = intent.getBooleanExtra("screen_state", true);

                if (!screenOn) {
                    // YOUR CODE

                    //aumnetamos el numerod e detecciones
                    numdetecciones++;

                    Log.e("PANTALLA ENCENDIDA !! num detecciones +1:", String.valueOf(numdetecciones));

                    // si encendemos pantalla dejamos de monitorizar
                    checkhaygente =false;


                    stopRecording();


                    //chequeamos cada 5 min que siga habiendo gente
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {

                        @Override
                        public void run() {
                            //Do something

                           //si l pnatalla esta encendida solo!!!



                            if (isScreenOn(mcontext)) {

                                Log.e("PANTALLA ENCENDIDA"," CHEQUEO GENTE ok");
                                checkhaygente =true;

                                numdetecciones--;
                                Log.i(TAG, "==============================timer de gente vencido resto 1 numdetecciones:"+numdetecciones);

                                if (numdetecciones>=1){

                                    //si no esta granbando q grabe:

                                    if (texture!=null) {

                                        Log.i(TAG, "==============================camara ya grabando no reinicio:");
                                    } else {



                                        startRecording();
                                    }
                                }

                                else {

                                    //no ha detectado a nadie 2 veces seguidas... anulo y apago



                                   // PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                  //  PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                       //     | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHESS");
                                   //apagamos pantalla

                                    if (!wl.isHeld()) {
                                        wl.release();
                                    }

                                    //dejamos grabar
                                    stopRecording();

                                    //dejamos de chequear people
                                    checkhaygente=false;


                                    //anulamos este timer
                                    if (timer != null){
                                         timer.cancel();
                                         timer=null; }



                                    //*apagamos a lo  bestia

                                    // Initiate DevicePolicyManager.
                                    mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
                                    mDPM.lockNow();


                                }

                            }

                            else {

                                if (timer != null){



                                timer.cancel();

                                    timer=null;

                                }




                            }


                        }
                    }, tiempoAutocheckpeople, tiempoAutocheckpeople);;//TODO timepo del timer en que chequea si hay gente


                } else {
                    // YOUR CODE
                    Log.e("PANTALLA APAGADA ", String.valueOf(screenOn));

                    // si apagamos  pantalla empezamos de monitorizar


                    if (timer != null){



                        timer.cancel();

                        timer=null;
                    }

                    if (wl.isHeld()) {


                        wl.release();

                    }


                    numdetecciones=0;

                    if (texture!=null) {

                        Log.i(TAG, "==============================camara ya grabando no reinicio:");
                    } else {


                        startRecording();
                    }


                }
            }

        }


        return Service.START_STICKY;
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean isCameraUsebyApp() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


 @Override
    public void onDestroy() {
        Log.w(TAG, "============Destroying CameraWatcherService");
        stopRecording();
        if(camera != null){
            camera.release();
        }
        notifier.cancel(NOTIFICATION_ID);


        //dreregistarmos el screen receiver

     unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    public void startRecording() {
        if (camera == null) {
            try {
                //camera = Camera.open();

                Log.d("INFO numero de camaras", String.valueOf(Camera.getNumberOfCameras()));///2

                //abrimos la frontal

                camera = Camera.open(1);

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


            Log.i(TAG, "==================Stopping to Record");
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


                //aumentamos num de de


                numdetecciones++;
                Log.i(TAG, "======================================= numdetecciones:"+numdetecciones);
                //stopRecording();

                //aqui habria que detecatr si encendemos la pantalla o seguimos detectadnod

                //startRecording();

                if (!checkhaygente) {


                    //numdetecciones=0;
                    Log.i(TAG, "==============================empiezza apk primera vez numdetecciones:"+numdetecciones);
                    empiezaAPKelegida();
                }

                else {


                    checkhaygente = false;


                    stopRecording();
                }





                /*
                //no quiero que me haga eso de momento
                Intent intent = new Intent(CameraWatcherService.this, MotionDetectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                */

            } else {
                camera.addCallbackBuffer(buffer);
                if (!toastPopped) {
                    toastPopped = true;
                    Toast.makeText(getBaseContext(), "Detectando de nuevo!!!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void empiezaAPKelegida() {

        //encndemos pantalla



        //enciende pero en lockscreen..solucion:quitamos el lock screen!!!

       // PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
       // PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
              //  | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHESS");
        wl.acquire();






        //lanzamos apk

        mPrefs= getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE);
        String nameapkelegida = mPrefs.getString("apkname", "No name defined");//"No name defined" is the default value.


        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(nameapkelegida);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( launchIntent );
    }

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
       // Intent resultIntent = new Intent(this, MotionDetectionActivity.class);

        Intent resultIntent = new Intent(this, AjustesNotificacionActivity.class);

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




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * Is the screen of the device on.
     * @param context the context
     * @return true when (at least one) screen is on
     */
    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {


                //   Log.i("***ESTADO DEL SCREEN:", String.valueOf(display.getState()));

                /*
                OJO CON AOD EL ESTADO ES 3 O 4
                https://developer.android.com/reference/android/view/Display.html#STATE_OFF

STATE_DOZE
added in API level 21
public static final int STATE_DOZE
Display state: The display is dozing in a low power state; it is still on but is optimized for showing system-provided content while the device is non-interactive.

See also:

getState()
PowerManager.isInteractive()
Constant Value: 3 (0x00000003)

STATE_DOZE_SUSPEND
added in API level 21
public static final int STATE_DOZE_SUSPEND
Display state: The display is dozing in a suspended low power state; it is still on but the CPU is not updating it. This may be used in one of two ways: to show static system-provided content while the device is non-interactive, or to allow a "Sidekick" compute resource to update the display. For this reason, the CPU must not control the display in this mode.

See also:

getState()
PowerManager.isInteractive()
Constant Value: 4 (0x00000004

                 */
                if (display.getState() != Display.STATE_OFF && display.getState() != Display.STATE_DOZE  && display.getState() != Display.STATE_DOZE_SUSPEND) {
                    screenOn = true;
                }
            }


            //  Log.i("***DEVUELVO  SCREEN:", String.valueOf(screenOn));

            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }





    private boolean isInQuietTime() {
        boolean quietTime = false;

        if(mPrefs.getBoolean("quiettime", false)) {
            // if(Myapplication.preferences.getBoolean(Myapplication.QuietTime, false)) {
            String startTime =  "22:59";
            String stopTime =  "08:00";

            //  Log.i("starquiettime: ",startTime);
            //  Log.i("stopquiettime:  ",stopTime);

            SimpleDateFormat sdfDate = new SimpleDateFormat("H:mm");
            String currentTimeStamp = sdfDate.format(new Date());
            int currentHour = Integer.parseInt(currentTimeStamp.split("[:]+")[0]);
            int currentMinute = Integer.parseInt(currentTimeStamp.split("[:]+")[1]);

            int startHour = Integer.parseInt(startTime.split("[:]+")[0]);
            int startMinute = Integer.parseInt(startTime.split("[:]+")[1]);

            int stopHour = Integer.parseInt(stopTime.split("[:]+")[0]);
            int stopMinute = Integer.parseInt(stopTime.split("[:]+")[1]);

            if (startHour < stopHour && currentHour > startHour && currentHour < stopHour) {
                quietTime = true;
            } else if (startHour > stopHour && (currentHour > startHour || currentHour < stopHour)) {
                quietTime = true;
            } else if (currentHour == startHour && currentMinute >= startMinute) {
                quietTime = true;
            } else if (currentHour == stopHour && currentMinute < stopMinute) {
                quietTime = true;
            }
        }

        //  Log.d(TAG,"Device is in quiet time: " + quietTime);
        return quietTime;
    }


}
