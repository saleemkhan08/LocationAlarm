package co.thnki.locationalarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

public class LocationAlarmApp extends MultiDexApplication
{
    private static Context context;
    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this.getApplicationContext();
    }

    public static SharedPreferences getPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void toast(String str)
    {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
    public static Context getAppContext()
    {
        return context;
    }
}
