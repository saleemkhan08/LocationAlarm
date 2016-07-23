package co.thnki.locationalarm.adapters;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import co.thnki.locationalarm.R;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.fragments.LocationAlarmListFragment;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.receivers.InternetConnectivityListener;
import co.thnki.locationalarm.services.LocationTrackingService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.ConnectivityUtil;
import co.thnki.locationalarm.utils.LocationUtil;
import co.thnki.locationalarm.viewholders.AdViewHolder;
import co.thnki.locationalarm.viewholders.ContentViewHolder;

import static java.lang.Math.abs;

public class ExpressNativeAdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private AppCompatActivity mActivity;
    private ArrayList<LocationAlarm> mAlarmPlusAdList;
    private static final int AD_VIEW = 125;
    private static final int CONTENT_VIEW = 124;
    private boolean isAdInserted;

    public ExpressNativeAdAdapter(AppCompatActivity activity)
    {
        mActivity = activity;
        Otto.register(this);
        mAlarmPlusAdList = new ArrayList<>();
    }

    public void unRegister()
    {
        Otto.unregister(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder;

        switch (viewType)
        {
            case AD_VIEW:
                View adParentView = inflater.inflate(R.layout.ad_row, parent, false);
                viewHolder = new AdViewHolder(adParentView);
                break;
            default:
                View contentParentView = inflater.inflate(R.layout.location_alarm_row, parent, false);
                viewHolder = new ContentViewHolder(contentParentView);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position)
    {
        int posType = (position - 1) % 5;

        if (!isAdInserted)
        {
            posType = -1;
        }
        switch (posType)
        {
            case 0:
                Log.d("GetItemType", posType + " : AD_VIEW : Pos : " + position);
                return AD_VIEW;
            default:
                Log.d("GetItemType", posType + " : CONTENT_VIEW : Pos : " + position);
                return CONTENT_VIEW;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
    {
        final LocationAlarm alarm = mAlarmPlusAdList.get(position);
        switch (getItemViewType(position))
        {
            case CONTENT_VIEW:
                if (null != alarm)
                {
                    configureContent(alarm, holder);
                }
                break;
        }
    }


    private void configureContent(final LocationAlarm alarm, RecyclerView.ViewHolder viewHolder)
    {
        final ContentViewHolder holder = (ContentViewHolder) viewHolder;
        holder.alarmAddress.setText(LocationUtil.getAddressLines(alarm.address, 3));
        holder.item.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Otto.post(alarm);
            }
        });

        holder.range.setText(getRadiusText(alarm.radius));
        if (alarm.status == LocationAlarm.ALARM_ON)
        {
            holder.cancelAlarm.setImageResource(R.mipmap.bell_slash_accent);
        }
        else
        {
            holder.cancelAlarm.setImageResource(R.mipmap.bell_accent);
        }

        holder.cancelAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int status = LocationAlarmDao.getAlarm(alarm.alarmId).status;
                if (status == LocationAlarm.ALARM_ON)
                {
                    setAlarm(alarm, false);
                    holder.cancelAlarm.setImageResource(R.mipmap.bell_accent);
                }
                else
                {
                    setAlarm(alarm, true);
                    holder.cancelAlarm.setImageResource(R.mipmap.bell_slash_accent);
                }
            }
        });

        holder.deleteAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LocationAlarmDao.delete(alarm.alarmId);
                updateList(LocationAlarmDao.getList());
            }
        });
    }

    private void setAlarm(LocationAlarm alarm, boolean isSet)
    {
        if (isSet)
        {
            LocationAlarmDao.update(alarm.alarmId, LocationAlarm.ALARM_ON);
            toast("Alarm Set : \n" + alarm.address);
        }
        else
        {
            LocationAlarmDao.update(alarm.alarmId, LocationAlarm.ALARM_OFF);
            toast("Alarm Turned off : \n" + alarm.address);
        }

        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        intent.putExtra(LocationTrackingService.KEY_ALARM_SET, true);
        mActivity.startService(intent);
    }

    private void toast(String str)
    {
        Toast toast = Toast.makeText(mActivity, str, Toast.LENGTH_LONG);
        ViewGroup view = (ViewGroup) toast.getView();
        ((TextView) view.getChildAt(0)).setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();
    }

    private String getRadiusText(int radius)
    {
        return "Range : " + ((radius >= 1000) ? (radius / 1000) + "km" : radius + "m");
    }

    @Override
    public int getItemCount()
    {
        return mAlarmPlusAdList.size();
    }

    @Subscribe
    public void onInternetConnected(String action)
    {
        switch (action)
        {
            case InternetConnectivityListener.INTERNET_CONNECTED:
                updateList(LocationAlarmDao.getList());
                Log.d("onInternetConnected", "notifyDataSetChanged");
                break;
        }
    }

    public void updateList(ArrayList<LocationAlarm> alarmList)
    {
        if (alarmList.size() < 1)
        {
            Otto.post(LocationAlarmListFragment.ALARM_LIST_EMPTY_TEXT);
        }
        else
        {
            Otto.post(LocationTrackingService.DELETE_ALARM_NOTIFICATION);
        }
        insertAdSpace(alarmList);
        notifyDataSetChanged();
    }

    private void insertAdSpace(ArrayList<LocationAlarm> alarmList)
    {
        isAdInserted = false;
        mAlarmPlusAdList = new ArrayList<>();
        if (!ConnectivityUtil.isConnected(mActivity))
        {
            mAlarmPlusAdList = alarmList;
        }
        else
        {
            isAdInserted = true;
            int size = alarmList.size();

            int finalSize = abs(size / 5) + size + 1;
            Log.d("insertAdSpace", "finalSize : " + finalSize + ", mAlarmList : " + size);

            if (size > 0)
            {
                for (int i = 0, j = 0; i < finalSize; i++)
                {
                    if (((i - 1) % 5) == 0)
                    {
                        Log.d("insertAdSpace", "Ad " + i);
                        mAlarmPlusAdList.add(i, null);
                    }
                    else
                    {
                        Log.d("insertAdSpace", "Content " + i);
                        mAlarmPlusAdList.add(i, alarmList.get(j++));
                    }
                }
            }
        }
    }
}