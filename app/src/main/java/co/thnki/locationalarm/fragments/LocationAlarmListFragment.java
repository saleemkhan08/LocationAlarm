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

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import co.thnki.locationalarm.LocationAlarmApp;
import co.thnki.locationalarm.MainActivity;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.adapters.AdMobNativeAdAdapter;
import co.thnki.locationalarm.adapters.AlarmAdapter;
import co.thnki.locationalarm.ads.expressads.AdMobExpressRecyclerAdapterWrapper;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.services.RemoteConfigService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.ImageUtil;
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
    private AdMobNativeAdAdapter mAdmobNativeAdAdapter;
    private AdMobExpressRecyclerAdapterWrapper mAdapterWrapper;

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

        String mAdUnitId = LocationAlarmApp.getPreferences()
                .getString(RemoteConfigService.AD_UNIT_ID + "2", "ca-app-pub-9949935976977846/3250322417");

        MobileAds.initialize(LocationAlarmApp.getAppContext(), mAdUnitId);

        mAdmobNativeAdAdapter = new AdMobNativeAdAdapter(mActivity, mAlarmList);

        mAdapterWrapper = new AdMobExpressRecyclerAdapterWrapper(mActivity);
        mAdapterWrapper.addTestDeviceId("51B143E236817102C0BC44F96EE8A5F7");

        mAdapterWrapper.setAdSize(new AdSize(ImageUtil.getAdWidth(mActivity) - 40, 300));
        mAdapterWrapper.setAdsUnitId(mAdUnitId);

        mAdapterWrapper.setAdapter(mAdmobNativeAdAdapter);

        mAdapterWrapper.setLimitOfAds(3);
        mAdapterWrapper.setNoOfDataBetweenAds(5);
        mAdapterWrapper.setFirstAdIndex(1);

        locationAlarmList.setAdapter(mAdapterWrapper);
        locationAlarmList.setLayoutManager(new LinearLayoutManager(mActivity));

        mAdmobNativeAdAdapter.notifyDataSetChanged();

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
        mAdmobNativeAdAdapter.unRegister();
        mAdapterWrapper.destroyAds();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
}
