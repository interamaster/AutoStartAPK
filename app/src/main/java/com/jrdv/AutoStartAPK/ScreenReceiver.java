package com.jrdv.AutoStartAPK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by esq00931 on 30/10/2016.
 *
 * https://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
 */

public class ScreenReceiver extends BroadcastReceiver {

    private boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
        }
        Intent i = new Intent(context, CameraWatcherService.class);
        i.putExtra("screen_state", screenOff);
        i.putExtra(CameraWatcherService.EXTRA_MESSAGE,"screen_state");
        context.startService(i);
    }

}