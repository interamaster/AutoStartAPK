package com.jrdv.AutoStartAPK;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.IBinder;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AjustesNotificacionActivity extends Activity {

    private static final String TAG = "AjustesNotificacionActivity";



    private SeekBar sensitivity;
    private int sensibilidadajuste;
    private TextView textosensibilidad;


    private SeekBar tiemporetardo;
    private int timepoajuste;
    private TextView textotimepo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes_notificacion);

        Log.i(TAG, "============================Starting AjustesNotificacionActivity");

        this.sensitivity = (SeekBar)findViewById(R.id.sensitivity);
        this.sensitivity.setOnSeekBarChangeListener(new AjustesNotificacionActivity.SensitivityChanger());

        this.textosensibilidad=(TextView) findViewById(R.id.sensibilidadtext);

//recuperamos valor seekbar


        SharedPreferences prefs = getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE);
        int valorajuste = prefs.getInt("ajustesensibilidad",  50);//"No name defined" is the default value.

        this.sensitivity.setProgress(valorajuste);

        this.textosensibilidad.setText("sensibilidad: "+valorajuste);





        this.tiemporetardo = (SeekBar)findViewById(R.id.tiemposeekbar);
        this.tiemporetardo.setOnSeekBarChangeListener(new AjustesNotificacionActivity.SensitivityChangertiempo());

        this.textotimepo=(TextView) findViewById(R.id.timepotext);



        //recuperamos valor seekbar



        int valorajustetimepo = prefs.getInt("ajustetiempo",  5);//"No name defined" is the default value.

        this.tiemporetardo.setProgress(valorajustetimepo);

        this.textotimepo.setText("tiempo(minutos): "+valorajustetimepo);



    }


    private class SensitivityChanger implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar sb, int i, boolean bln) {
            sensibilidadajuste=i;

            textosensibilidad.setText("sensibilidad: "+sensibilidadajuste);

        }

        @Override
        public void onStartTrackingTouch(SeekBar sb) {
            // do nothing?
        }

        @Override
        public void onStopTrackingTouch(SeekBar sb) {
            // do nothing?
        }

    }


    private class SensitivityChangertiempo implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar sb, int i, boolean bln) {
            timepoajuste=i;

            textotimepo.setText("tiempo(minutos): "+timepoajuste);

        }

        @Override
        public void onStartTrackingTouch(SeekBar sb) {
            // do nothing?
        }

        @Override
        public void onStopTrackingTouch(SeekBar sb) {
            // do nothing?
        }

    }



    @Override
    public void onPause() {
        super.onPause();
        if(!isFinishing()){
            finish();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(!isFinishing()){
            finish();
        }
    }



    @Override
    protected void onDestroy() {
        Log.i(TAG, "============ Destroying AjustesNotificacionActivity");

        SharedPreferences.Editor editor = getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt("ajustesensibilidad", sensibilidadajuste);
        editor.putInt("ajustetiempo", timepoajuste);
        editor.apply();


        super.onDestroy();
    }




    public void chooseapk(View view) {


        //aqui borramops el valor que habia y avisamo que lo vuelva a iniciar

        // MY_PREFS_NAME - a static String variable like:
        //public static final String MY_PREFS_NAME = "MyPrefsFile";
        SharedPreferences.Editor editor = getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("apkname", "porelegir");
        editor.apply();


        Toast.makeText(getBaseContext(), "Re-open me and choose apk again!!!", Toast.LENGTH_SHORT).show();


        finish();

    }


    public void stopWatching(View view){

        finish();
    }
}
