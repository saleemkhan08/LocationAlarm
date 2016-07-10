package co.thnki.locationalarm.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFcmListenerService extends FirebaseMessagingService
{
    private static final String TAG = "MyFcmListenerService";

    @Override
    public void onMessageReceived(RemoteMessage message)
    {
        String from = message.getFrom();
        Map data = message.getData();
        Log.d(TAG, "From : "+from+", data : "+ data);
    }
}
