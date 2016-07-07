package co.thnki.locationalarm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import co.thnki.locationalarm.singletons.Otto;

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
        if (intent.getExtras() != null)
        {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED)
            {
                /*Intent registrationService = new Intent( context, RegistrationIntentService.class);
                context.startService(registrationService);

                Intent getNotificationsService = new Intent(context, GetNotificationIntentService.class);
                context.startService(getNotificationsService);*/

                Otto.post(INTERNET_CONNECTED);
            }
            else
            {
                Otto.post(INTERNET_DISCONNECTED);
            }
        }
    }
}