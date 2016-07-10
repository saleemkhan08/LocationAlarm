package co.thnki.locationalarm.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import co.thnki.locationalarm.BuildConfig;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.interfaces.RemoteConfigFetchListener;

public class RemoteConfigUtil
{
    private static final String TAG = "RemoteConfigUtil";
    public static final java.lang.String PACKAGE_NAME = "newAppPkgName";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public RemoteConfigUtil()
    {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    public String getLatestVersionName()
    {
        return mFirebaseRemoteConfig.getString("latestVersionName");
    }


    public void fetchValuesFromServer(final RemoteConfigFetchListener listener)
    {
        long cacheExpiration = 0;
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "Fetch Succeeded");
                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                            listener.onFetched();
                        }
                        else
                        {
                            Log.d(TAG, "Fetch failed");
                            listener.onFailed();
                        }
                    }
                });
    }


    public String getNewAppPkgName()
    {
        return mFirebaseRemoteConfig.getString(PACKAGE_NAME);
    }

    public String getNewAppName()
    {
        return mFirebaseRemoteConfig.getString("newAppName");
    }

    public String getNewAppIcon()
    {
        return mFirebaseRemoteConfig.getString("newAppIcon");
    }

    public String getNotificationText()
    {
        return mFirebaseRemoteConfig.getString("notificationText");
    }
}
