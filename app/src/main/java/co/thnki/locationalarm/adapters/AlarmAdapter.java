package co.thnki.locationalarm.adapters;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.fragments.LocationAlarmListFragment;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.services.LocationTrackingService;
import co.thnki.locationalarm.singletons.Otto;

import static co.thnki.locationalarm.AlarmActivity.getAddressLines;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.PlaceViewHolder>
{
    private AppCompatActivity mActivity;
    private LayoutInflater inflater;
    private List<LocationAlarm> mAlarmList;

    public AlarmAdapter(AppCompatActivity activity, List<LocationAlarm> alarmList)
    {
        mActivity = activity;
        mAlarmList = alarmList;
        if (mAlarmList.size() == 0)
        {
            mAlarmList.add(0, null);
        }
        else
        {
            mAlarmList.add(1, null);
        }

        inflater = LayoutInflater.from(mActivity);
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.location_alarm_row, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder holder, final int position)
    {
        Log.d("alarmAdapter", "onBindViewHolder : " + position);
        final LocationAlarm alarm = mAlarmList.get(position);
        if (alarm == null)
        {
            Log.d("alarmAdapter", "Loading ad");
            holder.placeContent.setVisibility(View.GONE);
            AdRequest request = new AdRequest.Builder().addTestDevice("51B143E236817102C0BC44F96EE8A5F7").build();
            holder.nativeAdView.loadAd(request);
            holder.nativeAdView.setAdListener(new AdListener()
            {
                @Override
                public void onAdLoaded()
                {
                    super.onAdLoaded();
                    Log.d("alarmAdapter", "onAdLoaded");
                    holder.nativeAdViewWrapper.setVisibility(View.VISIBLE);
                }
            });
        }
        else
        {
            Log.d("alarmAdapter", "Loading alarm");
            holder.nativeAdViewWrapper.setVisibility(View.GONE);
            holder.placeContent.setVisibility(View.VISIBLE);

            holder.alarmAddress.setText(getAddressLines(alarm.address, 3));

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
                holder.cancelAlarm.setImageResource(R.mipmap.bell_cross_accent);
            }
            else
            {
                holder.cancelAlarm.setImageResource(R.mipmap.bell_icon_accent);
            }

            holder.cancelAlarm.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int status = LocationAlarmDao.getAlarm(alarm.address).status;
                    if (status == LocationAlarm.ALARM_ON)
                    {
                        setAlarm(alarm, false);
                        holder.cancelAlarm.setImageResource(R.mipmap.bell_icon_accent);
                    }
                    else
                    {
                        setAlarm(alarm, true);
                        holder.cancelAlarm.setImageResource(R.mipmap.bell_cross_accent);
                    }
                }
            });

            holder.deleteAlarm.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    LocationAlarmDao.delete(alarm.address);
                    removeAt(position);
                }
            });
        }
    }

    private void setAlarm(LocationAlarm alarm, boolean isSet)
    {
        if (isSet)
        {
            Log.d("FlowLogs", "setAlarm");
            LocationAlarmDao.update(alarm.address, LocationAlarm.ALARM_ON);
            toast("Alarm Set : \n" + alarm.address);
        }
        else
        {
            Log.d("FlowLogs", "resetAlarm");
            LocationAlarmDao.update(alarm.address, LocationAlarm.ALARM_OFF);
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

    private void removeAt(int position)
    {
        mAlarmList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAlarmList.size());
        if (mAlarmList.size() < 2)
        {
            Otto.post(LocationAlarmListFragment.ALARM_LIST_EMPTY_TEXT);
        }
        else
        {
            Otto.post(LocationTrackingService.DELETE_ALARM_NOTIFICATION);
        }
    }

    @Override
    public int getItemCount()
    {
        return mAlarmList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View item;

        @Bind(R.id.nativeAdView)
        NativeExpressAdView nativeAdView;

        @Bind(R.id.nativeAdViewContainer)
        RelativeLayout nativeAdViewWrapper;

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

        PlaceViewHolder(View itemView)
        {
            super(itemView);
            item = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}