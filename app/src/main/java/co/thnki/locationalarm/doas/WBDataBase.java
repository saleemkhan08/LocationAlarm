package co.thnki.locationalarm.doas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import co.thnki.locationalarm.LocationAlarmApp;

class WBDataBase
{
    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "locationAlarm";

    WBDataBase()
    {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(LocationAlarmApp.getAppContext(), 1);
        db = dataBaseHelper.getWritableDatabase();
    }

    private class DataBaseHelper extends SQLiteOpenHelper
    {
        String[] mTableSchema = {
                LocationAlarmDao.TABLE_SCHEMA
        };
        String[] mDropTable = {
                LocationAlarmDao.TABLE_SCHEMA
        };

        Context mContext;

        DataBaseHelper(Context context, int version)
        {
            super(context, DATABASE_NAME, null, version);
            mContext = context;

        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try
            {
                for (String schema : mTableSchema)
                {
                    db.execSQL(schema);
                }
            }
            catch (SQLException e)
            {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            try
            {
                for (String schema : mDropTable)
                {
                    db.execSQL(schema);
                }

                onCreate(db);
            }
            catch (SQLException e)
            {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    long insert(String tblname, ContentValues values)
    {
        Log.d("DatabaseProblem", "insert : tblname : " + tblname + ", values : " + values);
        return db.insertWithOnConflict(tblname, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    Cursor query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy)
    {
        return db.query(tableName, columns, selection, selectionArgs, groupBy, null, orderBy, null);
    }

    @SuppressWarnings("unused")
    Cursor query(String query)
    {
        return db.rawQuery(query, null);
    }

    long update(String tblname, ContentValues values, String whereClause, String[] whereArgs)
    {
        return db.update(tblname, values, whereClause, whereArgs);
    }

    long delete(String tblname, String whereClause, String[] whereArgs)
    {
        return db.delete(tblname, whereClause, whereArgs);
    }

    void closeDb()
    {
        db.close();
    }
}
