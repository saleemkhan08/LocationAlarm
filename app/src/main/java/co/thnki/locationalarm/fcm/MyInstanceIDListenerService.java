package co.thnki.locationalarm.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import co.thnki.locationalarm.LocationAlarmApp;


public class MyInstanceIDListenerService extends FirebaseInstanceIdService
{
    public static String TAG = "MyInstanceIDListener";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]

    @Override
    public void onTokenRefresh()
    {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        LocationAlarmApp.getPreferences().edit().putString(TAG, refreshedToken).apply();
        Log.d(TAG, "refreshedToken : "+refreshedToken);
    }
}
