package co.thnki.locationalarm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.thnki.locationalarm.services.RemoteConfigService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.ConnectivityUtil;

public class InternetConnectivityListener extends BroadcastReceiver
{
    public static final String INTERNET_CONNECTED = "INTERNET_CONNECTED";
    public static final String INTERNET_DISCONNECTED = "INTERNET_DISCONNECTED";

    public InternetConnectivityListener()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("LagIssue", "onReceive  : InternetConnectivityListener");
        if (intent.getExtras() != null)
        {
            if (ConnectivityUtil.isConnected(context))
            {
                Log.d("LagIssue", "INTERNET_CONNECTED  : InternetConnectivityListener");
                Otto.post(INTERNET_CONNECTED);
                context.startService(new Intent(context, RemoteConfigService.class));
            }
            else
            {
                Otto.post(INTERNET_DISCONNECTED);
            }
        }
    }
}