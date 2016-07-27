package co.thnki.locationalarm.doas;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import co.thnki.locationalarm.pojos.LocationAlarm;

public class LocationAlarmDao
{
    static final String TABLE_SCHEMA = "CREATE TABLE " + LocationAlarm.TABLE + " ("
            +LocationAlarm.ALARM_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LocationAlarm.ADDRESS + " VARCHAR(255), "
            + LocationAlarm.RADIUS + " INTEGER, "
            + LocationAlarm.LATITUDE + " VARCHAR(255), "
            + LocationAlarm.STATUS + " INTEGER, "
            + LocationAlarm.LONGITUDE + " VARCHAR(255));";

    public static void insert(LocationAlarm locationAlarm)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        if (null != locationAlarm)
        {
            ContentValues values = new ContentValues();
            values.put(LocationAlarm.ADDRESS, locationAlarm.address);
            values.put(LocationAlarm.LATITUDE, locationAlarm.latitude);
            values.put(LocationAlarm.LONGITUDE, locationAlarm.longitude);
            values.put(LocationAlarm.RADIUS, locationAlarm.radius);
            values.put(LocationAlarm.STATUS, locationAlarm.status);
            mWBDataBase.insert(LocationAlarm.TABLE, values);
        }
        mWBDataBase.closeDb();
    }

    @SuppressWarnings("unused")
    public static void delete()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(LocationAlarm.TABLE, null, null);
    }

    public static void delete(int alarmId)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(LocationAlarm.TABLE, LocationAlarm.ALARM_ID + " = ? ",new String[]{alarmId+""});
        mWBDataBase.closeDb();
    }

    public static ArrayList<LocationAlarm> getList()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ArrayList<LocationAlarm> alarmList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(LocationAlarm.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                LocationAlarm alarm = new LocationAlarm();
                alarm.alarmId = cursor.getInt(cursor.getColumnIndex(LocationAlarm.ALARM_ID));
                alarm.address = cursor.getString(cursor.getColumnIndex(LocationAlarm.ADDRESS));
                alarm.radius = cursor.getInt(cursor.getColumnIndex(LocationAlarm.RADIUS));
                alarm.latitude =  cursor.getString(cursor.getColumnIndex(LocationAlarm.LATITUDE));
                alarm.longitude =  cursor.getString(cursor.getColumnIndex(LocationAlarm.LONGITUDE));
                alarm.status = cursor.getInt(cursor.getColumnIndex(LocationAlarm.STATUS));
                alarmList.add(alarm);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return alarmList;
    }

    public static LocationAlarm getAlarm(int alarmId)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        LocationAlarm alarm = null;
        Cursor cursor = mWBDataBase.query(LocationAlarm.TABLE, null,  LocationAlarm.ALARM_ID + " = ? ", new String []{alarmId+""}, null, null);
        if (null != cursor)
        {
            if (cursor.moveToFirst())
            {
                alarm = new LocationAlarm();
                alarm.alarmId = cursor.getInt(cursor.getColumnIndex(LocationAlarm.ALARM_ID));
                alarm.address = cursor.getString(cursor.getColumnIndex(LocationAlarm.ADDRESS));
                alarm.radius = cursor.getInt(cursor.getColumnIndex(LocationAlarm.RADIUS));
                alarm.latitude =  cursor.getString(cursor.getColumnIndex(LocationAlarm.LATITUDE));
                alarm.longitude =  cursor.getString(cursor.getColumnIndex(LocationAlarm.LONGITUDE));
                alarm.status = cursor.getInt(cursor.getColumnIndex(LocationAlarm.STATUS));
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return alarm;
    }

    public static void update(int alarmId, int status)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ContentValues values = new ContentValues();
        values.put(LocationAlarm.STATUS, status);
        mWBDataBase.update(LocationAlarm.TABLE, values, LocationAlarm.ALARM_ID + " = ? ", new String []{alarmId+""});
        mWBDataBase.closeDb();
    }

    public static void cancelAllAlarms()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ContentValues values = new ContentValues();
        values.put(LocationAlarm.STATUS, LocationAlarm.ALARM_OFF);
        mWBDataBase.update(LocationAlarm.TABLE, values, null, null);
        mWBDataBase.closeDb();
    }
}