package com.jrdv.AutoStartAPK;

import android.app.Activity;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import static android.content.ContentValues.TAG;


//v0.1 compilado  para android studio ok de github:https://github.com/mienaikoe/DeltaMonitor
//v02 ya enciendo pantalla e inicia hangout
//v03 funciona timer ok pero el wlrelase no lo hace bien..



public class StartupActivity extends Activity {


    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private static final String TAG = "MotionDetectionActivity";

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //1<)=chequeamos si ya habia algo:
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String nameapkelegida = prefs.getString("apkname", "No name defined");//"No name defined" is the default value.



        //2) no habi nada ..creamos uno nuevo

        if (nameapkelegida.equals("No name defined") || nameapkelegida.equals("porelegir")) {

            // MY_PREFS_NAME - a static String variable like:
            //public static final String MY_PREFS_NAME = "MyPrefsFile";
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("apkname", "porelegir");
            editor.apply();


            Log.i(TAG, "===========================startup elegir nombre apk:");


        //y sacamos la activity de elegir apk





        }


        else{

            //ya hay apk elegida
            //TODO poder elgir otra apk aunque ya tengamos una elegida..ya hecho con un boton MotionDetectionActivity


        Intent intent = new Intent(StartupActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(CameraWatcherService.EXTRA_MESSAGE,"DesdeMain");
        startService(intent);
        
        finish();

        }
    }
    
    
   
        
}