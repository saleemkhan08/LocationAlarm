package co.thnki.locationalarm.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import co.thnki.locationalarm.AlarmActivity;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.interfaces.SettingsResultListener;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.receivers.NotificationActionReceiver;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.LocationUtil;
import co.thnki.locationalarm.utils.NotificationsUtil;
import co.thnki.locationalarm.utils.PermissionUtil;

public class LocationTrackingService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks
{
    public static final String STOP_SERVICE = "stopService";
    public static final String FORCE_STOP = "forceStop";
    public static final String TURN_ON_LOCATION_SETTINGS = "turnOnLocationSettings";
    public static final String DELETE_ALARM_NOTIFICATION = "DELETE_ALARM_NOTIFICATION";
    private static final String TAG = "LocationTrackingService";
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest mLocationSettingsRequest;

    public static final String KEY_TRAVELLING_MODE = "KEY_TRAVELLING_MODE";

    public static final String KEY_ALARM_SET = "KEY_ALARM_SET";
    private static final String KEY_LOCATION_UPDATE_FREQ = "updateFreq";
    private boolean mStartLocationUpdates;
    private LatLng mCurrentLatLng;

    public LocationTrackingService()
    {
        Log.d(TAG, "Service : Constructor");
    }

    private SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "Service : onBind");
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Otto.register(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        Log.d(TAG, "Service : onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        if (intent == null)
        {
            Log.d(TAG, "onStartCommand");
            if (!isStopServiceConditionMet())
            {
                if (PermissionUtil.isLocationPermissionAvailable() && !PermissionUtil.isLocationSettingsOn())
                {
                    String msg = "You have set a Location Alarm.\nPlease turn on Location settings?";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            if (intent.hasExtra(KEY_ALARM_SET))
            {
                Log.d(TAG, "KEY_ALARM_SET");
                preferences.edit().putBoolean(KEY_ALARM_SET, true).apply();
                communicator(DELETE_ALARM_NOTIFICATION);
            }
        }

        if (PermissionUtil.isLocationPermissionAvailable() && PermissionUtil.isLocationSettingsOn())
        {
            startLocationUpdates();
        }
        else
        {
            turnOnLocationSettings();
        }
        return START_STICKY;
    }

    private void turnOnLocationSettings()
    {
        PermissionUtil.turnOnLocationSettings(mGoogleApiClient, mLocationSettingsRequest, new SettingsResultListener()
        {
            @Override
            public void onLocationSettingsTurnedOn()
            {
                startLocationUpdates();
            }

            @Override
            public void onLocationSettingsCancelled()
            {
            }
        });
    }

    private void startLocationUpdates()
    {
        Log.d(TAG, "startLocationUpdates");
        if (mGoogleApiClient.isConnected())
        {
            int interval = Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "750"));
            mLocationRequest.setInterval(interval);
            mLocationRequest.setFastestInterval(interval);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else
        {
            mStartLocationUpdates = true;
        }
    }

    private void stopLocationUpdates()
    {
        Log.d(TAG, "stopLocationUpdates");
        if (mGoogleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mStartLocationUpdates = false;
    }

    @Subscribe
    public void communicator(String action)
    {
        Log.d("Communicator", action);
        switch (action)
        {
            case STOP_SERVICE:
                stopService();
                break;
            case FORCE_STOP:
                stopSelf();
                break;
            case TURN_ON_LOCATION_SETTINGS:
                turnOnLocationSettings();
                break;
            case DELETE_ALARM_NOTIFICATION:
                triggerAlarm(mCurrentLatLng);
                break;
        }
    }

    private LatLng getLatLng(Location mCurrentLocation)
    {
        return new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "Service : onDestroy");
        Otto.unregister(this);
        stopLocationUpdates();
    }

    private synchronized void buildGoogleApiClient()
    {
        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "500")));
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d(TAG, "Service  : onConnected : mStartLocationUpdates : " + mStartLocationUpdates);

        if (mStartLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d(TAG, "Service  : onConnectionSuspended");
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.d(TAG, "onLocationChanged");
        mCurrentLatLng = getLatLng(location);
        final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Otto.post(location);
        if (preferences.getBoolean(KEY_ALARM_SET, false))
        {
            triggerAlarm(currentLatLng);
        }
        stopService();
    }

    private void triggerAlarm(LatLng latLng)
    {
        ArrayList<LocationAlarm> alarms = LocationAlarmDao.getList();
        boolean allAlarmsStatus = false;
        String locations = "No Alarms Set...";
        int noOfLocations = 0;
        if (alarms.size() > 0)
        {
            for (LocationAlarm alarm : alarms)
            {
                if (alarm.status == LocationAlarm.ALARM_ON)
                {
                    double alarmLat = Double.parseDouble(alarm.latitude);
                    double alarmLng = Double.parseDouble(alarm.longitude);

                    double distance = (latLng != null) ? LocationUtil.distFrom(latLng.latitude, latLng.longitude, alarmLat, alarmLng)
                            : 1000000;

                    boolean currentAlarm = true;
                    if (distance < alarm.radius)
                    {
                        Intent intent = new Intent(this, AlarmActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(LocationAlarm.ALARM, alarm);
                        startActivity(intent);

                        Intent alarmAudioIntent = new Intent(this, AlarmAudioService.class);
                        startService(alarmAudioIntent);

                        currentAlarm = false;
                        LocationAlarmDao.update(alarm.alarmId, LocationAlarm.ALARM_OFF);
                    }
                    else
                    {
                        noOfLocations++;
                        if (noOfLocations > 1)
                        {
                            locations = "Click here to view the list...";
                        }
                        else
                        {
                            locations = LocationUtil.getAddressLines(alarm.address, 3);
                        }
                    }
                    allAlarmsStatus |= currentAlarm;
                }
            }
        }

        if (!allAlarmsStatus)
        {
            preferences.edit().putBoolean(KEY_ALARM_SET, false).apply();
            NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_LOCATION_ALARMS);
            stopService();
        }
        else
        {
            NotificationsUtil.showAlarmNotification(locations, noOfLocations);
        }
    }

    private void stopService()
    {
        Log.d(TAG, "stopService");
        if (isStopServiceConditionMet())
        {
            stopLocationUpdates();
            stopSelf();
            Log.d(TAG, "stopSelf");
        }
    }

    private boolean isStopServiceConditionMet()
    {
        boolean isAlarmSet = preferences.getBoolean(KEY_ALARM_SET, false);
        boolean isTravellingMode = preferences.getBoolean(KEY_TRAVELLING_MODE, false);
        if (!isAlarmSet && !isTravellingMode)
        {
            Log.d(TAG, "isStopServiceConditionMet  : true");
            return true;
        }
        else
        {
            Log.d(TAG, "isStopServiceConditionMet  : false");
            return false;
        }
    }
}