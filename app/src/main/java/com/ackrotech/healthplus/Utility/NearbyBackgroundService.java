package com.ackrotech.healthplus.Utility;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import androidx.annotation.Nullable;

public class NearbyBackgroundService extends IntentService
{
    private DBHelper dbHelper;
    public NearbyBackgroundService() {
        super("NearbyBackgroundService");
        Log.d("service","const");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = DBHelper.getInstance(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            Nearby.Messages.handleIntent(intent, new   MessageListener() {
                @Override
                public void onFound(Message message) {
                    Log.i("Service", "message found" + new String(message.getContent()));
                    try {
                        String msg = new String(message.getContent(), "UTF-8"); // for UTF-8 encoding
                        dbHelper.insertContact(msg);
                    }catch (Exception e){
                        Log.e("Service", e.getMessage());
                        e.printStackTrace();
                    }
                }
                @Override
                public void onLost(Message message) {
                    Log.i("Service", "lost message = " + message);
                }
            });
        }
    }
}