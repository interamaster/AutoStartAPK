package com.jrdv.AutoStartAPK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


//v0.1 compilado  para android studio ok de github:https://github.com/mienaikoe/DeltaMonitor
//v02 ya enciendo pantalla e inicia hangout
//v03 funciona timer ok pero el wlrelase no lo hace bien..
//v04 menu elgir apk listo,,falta eñgirlo



public class StartupActivity extends Activity {


    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private static final String TAG = "MotionDetectionActivity";


    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listadaptor = null;
    private ListView listView;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chooseapkactivity);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.list);
        packageManager = getPackageManager();
        new LoadApplications().execute();



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





           // finish();

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



    class LoadApplications extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress = null;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(StartupActivity.this, null, "Loading applications...");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            listadaptor = new ApplicationAdapter(StartupActivity.this,
                    R.layout.row_item, applist);

            return null;
        }

        private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
            ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
            for (ApplicationInfo info : list) {
                try {
                    if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                        applist.add(info);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return applist;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listView.setAdapter(listadaptor);
            progress.dismiss();
            super.onPostExecute(aVoid);
        }
    }
   
        
}