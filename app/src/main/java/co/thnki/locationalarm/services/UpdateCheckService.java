package co.thnki.locationalarm.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import co.thnki.locationalarm.BuildConfig;
import co.thnki.locationalarm.LocationAlarmApp;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.interfaces.RemoteConfigFetchListener;
import co.thnki.locationalarm.pojos.NotificationData;
import co.thnki.locationalarm.receivers.NotificationActionReceiver;
import co.thnki.locationalarm.utils.NotificationsUtil;
import co.thnki.locationalarm.utils.RemoteConfigUtil;

public class UpdateCheckService extends Service implements RemoteConfigFetchListener
{
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private RemoteConfigUtil mRemoteConfigUtil;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mRemoteConfigUtil = new RemoteConfigUtil();
        mRemoteConfigUtil.fetchValuesFromServer(this);
        return START_NOT_STICKY;
    }

    @Override
    public void onFetched()
    {
        SharedPreferences preferences = LocationAlarmApp.getPreferences();
        String versionName = mRemoteConfigUtil.getLatestVersionName();
        String newAppPkgName = mRemoteConfigUtil.getNewAppPkgName();
        if (!BuildConfig.VERSION_NAME.equals(versionName))
        {
            if (preferences.getBoolean(versionName, false))
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
                NotificationsUtil.showNotification(data);
                preferences.edit().putString(RemoteConfigUtil.PACKAGE_NAME, getPackageName()).apply();
            }
        }
        else if (!preferences.contains(newAppPkgName))
        {
            if(!isAppInstalled(newAppPkgName))
            {
                Log.d("UpdateCheckService", "getNewAppPkgName : " + newAppPkgName);
                NotificationData data = new NotificationData();
                preferences.edit().putString(RemoteConfigUtil.PACKAGE_NAME, newAppPkgName).apply();
                data.action1IntentIcon = R.mipmap.install_grey;
                data.action1IntentTag = NotificationActionReceiver.UPDATE_APP;
                data.action1IntentText = "Install";

                data.largeIconUrl = mRemoteConfigUtil.getNewAppIcon();
                data.contentIntentTag = NotificationActionReceiver.UPDATE_APP;
                data.contentTitle = mRemoteConfigUtil.getNewAppName();
                data.contentText = mRemoteConfigUtil.getNotificationText();

                data.action2IntentIcon = R.mipmap.reject_grey;
                data.action2IntentTag = NotificationActionReceiver.CANCEL_UPDATE;
                data.action2IntentText = "Cancel";
                data.notificationId = NotificationActionReceiver.NOTIFICATION_ID_APP_UPDATE;
                NotificationsUtil.showNotification(data);
            }
        }
    }

    @Override
    public void onFailed()
    {

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
