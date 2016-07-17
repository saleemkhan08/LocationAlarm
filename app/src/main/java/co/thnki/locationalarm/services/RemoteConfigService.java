package co.thnki.locationalarm.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import co.thnki.locationalarm.BuildConfig;
import co.thnki.locationalarm.LocationAlarmApp;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.pojos.NotificationData;
import co.thnki.locationalarm.receivers.NotificationActionReceiver;
import co.thnki.locationalarm.utils.NotificationsUtil;

public class RemoteConfigService extends Service
{
    public static final String AD_UNIT_ID = "adUnitId";
    private static final String LATEST_VERSION_NAME = "latestVersionName";
    private static final String NEW_APP_ICON = "newAppIcon";
    private static final String NEW_APP_NAME = "newAppName";
    private static final String NOTIFICATION_TEXT = "notificationText";
    public static final String PACKAGE_NAME = "newAppPkgName";
    private static final String TAG = "UpdateCheckService";

    private SharedPreferences mSharedPreferences;
    private FirebaseRemoteConfig mFireBaseRemoteConfig;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)//TODO remove in release apk
                .build();

        mFireBaseRemoteConfig.setConfigSettings(configSettings);
        mFireBaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        mSharedPreferences = LocationAlarmApp.getPreferences();

        long cacheExpiration = 0;
        mFireBaseRemoteConfig.fetch(cacheExpiration)
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
                            mFireBaseRemoteConfig.activateFetched();
                            onFetched();
                        }
                        else
                        {
                            Log.d(TAG, "Fetch failed");
                        }
                    }
                });
        return START_NOT_STICKY;
    }

    private void onFetched()
    {
        String versionName = mFireBaseRemoteConfig.getString(LATEST_VERSION_NAME);
        String newAppPkgName = mFireBaseRemoteConfig.getString(PACKAGE_NAME);
        String newAppIcon = mFireBaseRemoteConfig.getString(NEW_APP_ICON);
        String newAppName = mFireBaseRemoteConfig.getString(NEW_APP_NAME);
        String notificationText = mFireBaseRemoteConfig.getString(NOTIFICATION_TEXT);

        if (!BuildConfig.VERSION_NAME.equals(versionName))
        {
            Log.d("UpdateCheckService", "Update Available");
            NotificationData data = new NotificationData();
            data.action1IntentIcon = R.mipmap.install_grey;
            data.action1IntentTag = NotificationActionReceiver.UPDATE_APP;
            data.action1IntentText = "Install";
            data.contentIntentTag = NotificationActionReceiver.UPDATE_APP;
            data.contentText = "New version is available!";
            data.contentTitle = "Location Alarm";
            data.action2IntentIcon = R.mipmap.reject_grey;
            data.action2IntentTag = NotificationActionReceiver.CANCEL_UPDATE;
            data.action2IntentText = "Cancel";
            data.notificationId = NotificationActionReceiver.NOTIFICATION_ID_APP_UPDATE;
            data.vibrate = true;
            NotificationsUtil.showNotification(data);
        }
        else if (!mSharedPreferences.contains(newAppPkgName))
        {
            if (!isAppInstalled(newAppPkgName))
            {
                Log.d("UpdateCheckService", "getNewAppPkgName : " + newAppPkgName);
                NotificationData data = new NotificationData();
                data.action1IntentIcon = R.mipmap.install_grey;
                data.action1IntentTag = NotificationActionReceiver.UPDATE_APP;
                data.action1IntentText = "Install";

                data.largeIconUrl = newAppIcon;
                data.contentIntentTag = NotificationActionReceiver.UPDATE_APP;
                data.contentTitle = newAppName;
                data.contentText = notificationText;

                data.action2IntentIcon = R.mipmap.reject_grey;
                data.action2IntentTag = NotificationActionReceiver.CANCEL_UPDATE;
                data.action2IntentText = "Cancel";
                data.notificationId = NotificationActionReceiver.NOTIFICATION_ID_APP_UPDATE;
                data.vibrate = true;
                NotificationsUtil.showNotification(data);
            }
        }
        mSharedPreferences.edit()
                .putString(PACKAGE_NAME, newAppPkgName)
                .putString(AD_UNIT_ID + "1", mFireBaseRemoteConfig.getString("adUnitId1"))
                .putString(AD_UNIT_ID + "2", mFireBaseRemoteConfig.getString("adUnitId2"))
                .putString(AD_UNIT_ID + "3", mFireBaseRemoteConfig.getString("adUnitId3"))
                .apply();
        stopSelf();
    }

    private boolean isAppInstalled(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try
        {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed;
    }
}
