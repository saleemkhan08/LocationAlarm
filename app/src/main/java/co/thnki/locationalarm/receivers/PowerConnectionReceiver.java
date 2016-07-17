package co.thnki.locationalarm.receivers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import co.thnki.locationalarm.services.InternetJobService;

public class PowerConnectionReceiver extends WakefulBroadcastReceiver
{
    private static int sJobId;

    public PowerConnectionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("LagIssue", "onReceive  : PowerConnectionReceiver");
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        /*int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;*/

        if (isCharging)
        {
            Log.d("PowerConnection", "Charging");
            if (Build.VERSION.SDK_INT >= 21)
            {
                ComponentName mServiceComponent = new ComponentName(context, InternetJobService.class);
                JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(sJobId++, mServiceComponent);

                //builder.setMinimumLatency(1000);//when to start the jaob after it is triggered
                //builder.setOverrideDeadline(1000); //With in how much time job can be finished
                //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // only when wifi is available

                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);//when any network is available.
                builder.setPersisted(true); //persist over reboot.
                builder.setRequiresDeviceIdle(false); //when screen is off
                builder.setRequiresCharging(false); // when phone is put for charging
                scheduler.schedule(builder.build());

            }
        }else
        {
            Log.d("PowerConnection", "Not Charging");
        }
    }
}
