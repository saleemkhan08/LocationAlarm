package co.thnki.locationalarm.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.ads.RecyclerViewAdapterBase;
import co.thnki.locationalarm.ads.ViewWrapper;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.fragments.LocationAlarmListFragment;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.services.LocationTrackingService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.LocationUtil;


public class AdMobNativeAdAdapter extends RecyclerViewAdapterBase<LocationAlarm, AdMobNativeAdAdapter.PlaceViewHolder>
{
    private AppCompatActivity mActivity;
    private List<LocationAlarm> mAlarmList;

    public AdMobNativeAdAdapter(AppCompatActivity activity, List<LocationAlarm> alarmList)
    {
        mActivity = activity;
        mAlarmList = alarmList;
        Otto.register(this);
    }

    public void unRegister()
    {
        Otto.unregister(this);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<PlaceViewHolder> viewHolderWrapper, final int position)
    {
        final PlaceViewHolder holder = viewHolderWrapper.getView();
        Log.d("alarmAdapter", "onBindViewHolder : " + position);
        final LocationAlarm alarm = mAlarmList.get(position);

        Log.d("alarmAdapter", "Loading alarm");
        holder.placeContent.setVisibility(View.VISIBLE);
        holder.alarmAddress.setText(LocationUtil.getAddressLines(alarm.address, 3));

        holder.setOnClickListener(new View.OnClickListener()
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
                removeAt(position);
            }
        });
    }

    private void setAlarm(LocationAlarm alarm, boolean isSet)
    {
        if (isSet)
        {
            Log.d("FlowLogs", "setAlarm");
            LocationAlarmDao.update(alarm.alarmId, LocationAlarm.ALARM_ON);
            toast("Alarm Set : \n" + alarm.address);
        }
        else
        {
            Log.d("FlowLogs", "resetAlarm");
            LocationAlarmDao.update(alarm.alarmId, LocationAlarm.ALARM_OFF);
            toast("Alarm Turned off : \n" + alarm.address);
        }

        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        intent.putExtra(LocationTrackingService.KEY_ALARM_SET, true);
        mActivity.startService(intent);

    }

    @Override
    public int getItemCount()
    {
        return mAlarmList.size();
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

    private void removeAt(int position)
    {
        mAlarmList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAlarmList.size());
        if (mAlarmList.size() < 1)
        {
            Otto.post(LocationAlarmListFragment.ALARM_LIST_EMPTY_TEXT);
        }
        else
        {
            Otto.post(LocationTrackingService.DELETE_ALARM_NOTIFICATION);
        }
    }

    @Override
    protected PlaceViewHolder onCreateItemView(ViewGroup parent, int viewType)
    {
        return new PlaceViewHolder(mActivity);
    }

    @Override
    public LocationAlarm getItem(int position)
    {
        return mAlarmList.get(position);
    }

    class PlaceViewHolder extends CardView
    {
        @Bind(R.id.alarmAddress)
        TextView alarmAddress;

        @Bind(R.id.range)
        TextView range;

        @Bind(R.id.cancelAlarm)
        ImageView cancelAlarm;

        @Bind(R.id.deleteAlarm)
        ImageView deleteAlarm;

        @Bind(R.id.placeContent)
        RelativeLayout placeContent;

        PlaceViewHolder(Context context)
        {
            super(context);
            inflate(context, R.layout.location_alarm_row, this);
            ButterKnife.bind(this, this);
        }
    }
}
