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
import android.preference.Preference;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.CheckBox;
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

    private CheckBox opcionquiettime;

    private  SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes_notificacion);

        Log.i(TAG, "============================Starting AjustesNotificacionActivity");

        this.sensitivity = (SeekBar)findViewById(R.id.sensitivity);
        this.sensitivity.setOnSeekBarChangeListener(new AjustesNotificacionActivity.SensitivityChanger());

        this.textosensibilidad=(TextView) findViewById(R.id.sensibilidadtext);

//recuperamos valor seekbar


        mPrefs= getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE);
        int valorajuste = mPrefs.getInt("ajustesensibilidad",  50);//"No name defined" is the default value.

        this.sensitivity.setProgress(valorajuste);

        this.textosensibilidad.setText("sensibilidad a mayor valor mas sensibilidad: "+valorajuste);





        this.tiemporetardo = (SeekBar)findViewById(R.id.tiemposeekbar);
        this.tiemporetardo.setOnSeekBarChangeListener(new AjustesNotificacionActivity.SensitivityChangertiempo());

        this.textotimepo=(TextView) findViewById(R.id.timepotext);



        //recuperamos valor seekbar



        int valorajustetimepo = mPrefs.getInt("ajustetiempo",  5);//"No name defined" is the default value.

        this.tiemporetardo.setProgress(valorajustetimepo);

        this.textotimepo.setText("tiempo(minutos): "+valorajustetimepo);



        //defeinimo el check box

        opcionquiettime = (CheckBox)findViewById(R.id.checktime);



    }


    public void quiettimecheck(View view) {

        SharedPreferences.Editor editor = getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();

        if(opcionquiettime.isChecked()){

            mPrefs = getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE);

            //chequeamos quiettime

            boolean quiettime = mPrefs.getBoolean("quiettime",  true);//"No name defined" is the default value.



            editor.putBoolean("quiettime",  true);
            editor.apply();



            Log.d("INFO", "quiet time habilitado:");



        }else{



            editor.putBoolean("quiettime",  false);
            editor.apply();



            Log.d("INFO", "quiet time deshabilitado:");

        }


    }


    private class SensitivityChanger implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar sb, int i, boolean bln) {
            sensibilidadajuste=i;

            textosensibilidad.setText("sensibilidad a mayor valor mas sensibilidad: "+sensibilidadajuste);

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


        Intent intent = new Intent(AjustesNotificacionActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(CameraWatcherService.EXTRA_MESSAGE,"DesdeAjustes");
        startService(intent);




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

        Intent intent = new Intent(AjustesNotificacionActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(CameraWatcherService.EXTRA_MESSAGE,"DesdeAjustes");
        startService(intent);

        finish();
    }
}
