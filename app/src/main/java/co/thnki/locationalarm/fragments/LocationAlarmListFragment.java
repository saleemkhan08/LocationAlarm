package co.thnki.locationalarm.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import co.thnki.locationalarm.MainActivity;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.adapters.AlarmAdapter;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.TransitionUtil;

public class LocationAlarmListFragment extends Fragment
{
    public static final String ALARM_LIST_EMPTY_TEXT = "ALARM_LIST_EMPTY_TEXT";
    public static final String RELOAD_LIST = "reloadList";

    @BindString(R.string.noLocationAlarmsAreSet)
    String youHaventSetAnyLocationAlarm;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;
    private AppCompatActivity mActivity;
    private AlarmAdapter mAlarmAdapter;

    public LocationAlarmListFragment()
    {
    }

    @Bind(R.id.locationAlarmList)
    RecyclerView locationAlarmList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_location_alarm_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        ArrayList<LocationAlarm> mAlarmList = LocationAlarmDao.getList();
        mActivity = (AppCompatActivity) getActivity();
        if (mAlarmList.size() < 1)
        {
            showEmptyListString();
        }

        mAlarmAdapter = new AlarmAdapter(mActivity, mAlarmList);
        locationAlarmList.setAdapter(mAlarmAdapter);
        locationAlarmList.setLayoutManager(new LinearLayoutManager(mActivity));
        return parentView;
    }

    @Subscribe
    public void showEmptyListString(String msg)
    {
        Log.d(MainActivity.TAG, msg);
        switch (msg)
        {
            case ALARM_LIST_EMPTY_TEXT:
                showEmptyListString();
                emptyListTextView.setText(youHaventSetAnyLocationAlarm);
                break;
            case RELOAD_LIST :
                ArrayList<LocationAlarm> mAlarmList = LocationAlarmDao.getList();
                if (mAlarmList.size() < 1)
                {
                    showEmptyListString();
                }
                else
                {
                    hideEmptyListString();
                }
                locationAlarmList.setAdapter(new AlarmAdapter(mActivity, mAlarmList));

                break;
        }
    }

    private void hideEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.GONE);
    }

    private void showEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
        mAlarmAdapter.unRegister();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
}
