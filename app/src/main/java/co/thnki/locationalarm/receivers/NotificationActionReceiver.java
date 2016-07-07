package co.thnki.locationalarm.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.thnki.locationalarm.MainActivity;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.fragments.LocationAlarmListFragment;
import co.thnki.locationalarm.singletons.Otto;

public class NotificationActionReceiver extends BroadcastReceiver
{
    public static final String NOTIFICATION_ACTION = "notificationAction";
    public static final String CANCEL_ALL_ALARMS = "CancelAllAlarms";
    public static final int NOTIFICATION_ID_LOCATION_ALARMS = 181;

    public NotificationActionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getStringExtra(NOTIFICATION_ACTION);
        Log.d(NOTIFICATION_ACTION, "NOTIFICATION_ACTION : " + action);
        switch (action)
        {
            case CANCEL_ALL_ALARMS:
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                LocationAlarmDao.cancelAllAlarms();
                notificationManager.cancel(NOTIFICATION_ID_LOCATION_ALARMS);
                Otto.post(LocationAlarmListFragment.RELOAD_LIST);
                break;

            case MainActivity.TAG:
                Intent intentMainActivity = new Intent(context, MainActivity.class);
                intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentMainActivity);
                break;
        }
    }
}
