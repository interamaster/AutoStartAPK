package com.jrdv.AutoStartAPK;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;


//v0.1 compilado  para android studio ok de github:https://github.com/mienaikoe/DeltaMonitor
//v02 ya enciendo pantalla e inicia hangout
//v03 funciona timer ok pero el wlrelase no lo hace bien..



public class StartupActivity extends Activity {


    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(StartupActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(CameraWatcherService.EXTRA_MESSAGE,"DesdeMain");
        startService(intent);
        
        finish();
    }
    
    
   
        
}