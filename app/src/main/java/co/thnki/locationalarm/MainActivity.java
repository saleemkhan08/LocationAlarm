package co.thnki.locationalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import co.thnki.locationalarm.adapters.ExpressNativeAdAdapter;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.fragments.MapFragment;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.receivers.InternetConnectivityListener;
import co.thnki.locationalarm.services.RemoteConfigService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.LocationUtil;
import co.thnki.locationalarm.utils.TransitionUtil;
import co.thnki.locationalarm.viewholders.AdViewHolder;

import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.COLLAPSED;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED;

public class MainActivity extends AppCompatActivity
{
    public static final String TAG = "LocationAlarmMain";
    private static final String MAP_FRAGMENT = "mapFragment";
    private static final String LOCATION_ALARM_LIST_FRAGMENT = "locationAlarmListFragment";

    @Bind(R.id.sliding_layout)
    SlidingUpPanelLayout mLayout;

    @Bind(R.id.mapAdView)
    RelativeLayout mMapAdView;

    @Bind(R.id.descriptionText)
    TextView mDescriptionText;

    @Bind(R.id.scrollUp)
    ImageView scrollUp;

    @BindColor(R.color.colorAccent)
    int mAccentColor;

    @BindColor(R.color.my_location_radius)
    int mRadiusColor;

    @Bind(R.id.mapContainer)
    ViewGroup mMapFragmentView;

    @Bind(R.id.titleText)
    TextView titleText;

    @Bind(R.id.titleBar)
    RelativeLayout mTitleBar;
    private MapFragment mMapFragment;


    private GoogleApiClient client;
    @BindString(R.string.tapToViewList)
    String mTapToViewTheList;

    @BindString(R.string.tapToViewMap)
    String mTapToViewTheMap;
    private SharedPreferences mPreferences;
    private NativeExpressAdView mAdView;
    private SlidingUpPanelLayout.PanelState mSlidingToolbarState;

    /**
     * Fragment Content
     */

    public static final String ALARM_LIST_EMPTY_TEXT = "ALARM_LIST_EMPTY_TEXT";
    public static final String RELOAD_LIST = "reloadList";

    @BindString(R.string.noLocationAlarmsAreSet)
    String youHaventSetAnyLocationAlarm;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;
    private ExpressNativeAdAdapter mExpressNativeAdAdapter;

    @Bind(R.id.locationAlarmList)
    RecyclerView mLocationAlarmList;

    @Bind(R.id.card_view)
    CardView adCardView;

    /**
     * End of Fragment Content
     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("LagIssue", "onCreate  : MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sliding_up);
        mPreferences = LocationAlarmApp.getPreferences();
        ButterKnife.bind(this);
        Otto.register(this);
        setUpSlidingToolbar();

        initializeAppBarLayout();
        initializeAppIndexing();
        initializeMapAd();
        if (Build.VERSION.SDK_INT >= 21)
        {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        alarmListFragment();
    }

    /**
     * Fragment Content
     */

    private void alarmListFragment()
    {
        ArrayList<LocationAlarm> mAlarmList = LocationAlarmDao.getList();
        if (mAlarmList.size() < 1)
        {
            showEmptyListString();
        }
        String mAdUnitId = LocationAlarmApp.getPreferences()
                .getString(RemoteConfigService.AD_UNIT_ID + "2", "ca-app-pub-9949935976977846/3250322417");

        MobileAds.initialize(LocationAlarmApp.getAppContext(), mAdUnitId);

        mExpressNativeAdAdapter = new ExpressNativeAdAdapter(this);
        mLocationAlarmList.setAdapter(mExpressNativeAdAdapter);
        mLocationAlarmList.setLayoutManager(new LinearLayoutManager(this));
        mExpressNativeAdAdapter.updateList(mAlarmList);
    }

    private void loadEmptyListAd()
    {
        new AdViewHolder(adCardView);
    }

    private void hideEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.GONE);
        mLocationAlarmList.setVisibility(View.VISIBLE);
    }

    private void showEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
        mLocationAlarmList.setVisibility(View.GONE);
        loadEmptyListAd();
    }

    /**
     * End of Fragment Content
     */
    private void initializeMapAd()
    {
        mAdView = new NativeExpressAdView(this);
        mAdView.setAdSize(new AdSize(320, 80));
        mAdView.setAdUnitId(mPreferences.getString(RemoteConfigService.AD_UNIT_ID + 1, "ca-app-pub-9949935976977846/4866656411"));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(layoutParams);
        layout.addView(mAdView);
        mMapAdView.addView(layout);
    }

    private void initializeAppBarLayout()
    {
        FragmentManager manager = getSupportFragmentManager();
        addMapFragment(manager);
        TextView title = (TextView) findViewById(R.id.titleText);
        title.setTypeface(LocationAlarmApp.getTypeFace());
    }

    private void initializeAppIndexing()
    {
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    private void addMapFragment(FragmentManager manager)
    {
        Log.d("LagIssue", "addMapFragment  : MainActivity");
        mMapFragment = new MapFragment();
        manager.beginTransaction()
                .replace(R.id.mapContainer, mMapFragment, MAP_FRAGMENT)
                .commit();
    }

    private void loadAd()
    {
        Log.d("LagIssue", "loadAd  : MainActivity");
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("51B143E236817102C0BC44F96EE8A5F7")
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                TransitionManager.beginDelayedTransition(mMapAdView);
                mMapAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        loadAd();
    }

    @Override
    public void onBackPressed()
    {
        if (mSlidingToolbarState == EXPANDED)
        {
            mLayout.setPanelState(COLLAPSED);
        }
        else if (mMapFragment != null && mMapFragment.isSubmitButtonShown)
        {
            mMapFragment.hideSubmitButtonAndShowAddButton();
        }
        else
        {
            super.onBackPressed();
        }
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Otto.post(RELOAD_LIST);
    }


    @Subscribe
    public void onAlarmClicked(LocationAlarm action)
    {
        mLayout.setPanelState(COLLAPSED);
        mMapFragment.gotoLatLng(LocationUtil.getLatLng(action.latitude, action.longitude), true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
        /**
         *
         */
        mExpressNativeAdAdapter.unRegister();
        /**
         *
         */
    }

    @Subscribe
    public void onInternetConnected(String action)
    {
        Log.d("ConnectivityListener", "onInternetConnected : main activity : " + action);
        switch (action)
        {
            case InternetConnectivityListener.INTERNET_CONNECTED:
                loadAd();
                break;
            /**
             *
             */
            case ALARM_LIST_EMPTY_TEXT:
                showEmptyListString();
                emptyListTextView.setText(youHaventSetAnyLocationAlarm);
                Log.d("ConnectivityListener", ALARM_LIST_EMPTY_TEXT + ": Called");
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
            /**
             *
             */
        }
    }

    private Action getIndexApiAction()
    {
        Thing object = new Thing.Builder()
                .setName(getString(R.string.app_name))
                .setUrl(Uri.parse("http://www.thnki.co"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private void setUpSlidingToolbar()
    {

        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener()
        {
            @Override
            public void onPanelSlide(View panel, float slideOffset)
            {
                if (slideOffset > 0.5)
                {
                    mDescriptionText.setText(mTapToViewTheMap);
                    scrollUp.setImageResource(R.mipmap.scroll_down);
                }
                else
                {
                    mDescriptionText.setText(mTapToViewTheList);
                    scrollUp.setImageResource(R.mipmap.scroll_up);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState)
            {
                //Log.i(TAG, "onPanelStateChanged " + newState);
                mSlidingToolbarState = newState;
                if (newState == COLLAPSED)
                {
                    Otto.post(MapFragment.DIALOG_DISMISS);
                }
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mLayout.setPanelState(COLLAPSED);
            }
        });
    }
}