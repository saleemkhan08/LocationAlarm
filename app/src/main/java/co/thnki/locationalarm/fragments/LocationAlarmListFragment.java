package co.thnki.locationalarm.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import co.thnki.locationalarm.LocationAlarmApp;
import co.thnki.locationalarm.MainActivity;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.adapters.ExpressNativeAdAdapter;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.services.RemoteConfigService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.TransitionUtil;
import co.thnki.locationalarm.viewholders.AdViewHolder;

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
    private ExpressNativeAdAdapter mExpressNativeAdAdapter;

    public LocationAlarmListFragment()
    {
    }

    @Bind(R.id.locationAlarmList)
    RecyclerView mLocationAlarmList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_location_alarm_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        ArrayList<LocationAlarm> mAlarmList = LocationAlarmDao.getList();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if (mAlarmList.size() < 1)
        {
            showEmptyListString();
        }

        String mAdUnitId = LocationAlarmApp.getPreferences()
                .getString(RemoteConfigService.AD_UNIT_ID + "2", "ca-app-pub-9949935976977846/3250322417");

        MobileAds.initialize(LocationAlarmApp.getAppContext(), mAdUnitId);

        mExpressNativeAdAdapter = new ExpressNativeAdAdapter(mActivity);
        mLocationAlarmList.setAdapter(mExpressNativeAdAdapter);
        mLocationAlarmList.setLayoutManager(new LinearLayoutManager(mActivity));
        mExpressNativeAdAdapter.updateList(mAlarmList);
        return parentView;
    }


    @Bind(R.id.listNativeAdViewContainer)
    RelativeLayout adContainer;

    @Bind(R.id.card_view)
    CardView adCardView;

    private void loadEmptyListAd()
    {
        new AdViewHolder(adCardView);
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
                mExpressNativeAdAdapter.updateList(mAlarmList);
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
        loadEmptyListAd();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
        mExpressNativeAdAdapter.unRegister();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
}
