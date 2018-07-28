package com.vikas.quizhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = false;

    public Context context;

    public ScreenReceiver(Context context){
        this.context=context;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            Log.e("MYAP", "SCREEN TURNED OFF");
             wasScreenOn = false;
            Intent intent1=new Intent(context,BubbleService.class);
            context.stopService(intent1);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            Log.e("MYAP", "SCREEN TURNED ON");
            wasScreenOn = true;
        }
    }

}
