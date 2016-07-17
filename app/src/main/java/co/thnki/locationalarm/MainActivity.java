package co.thnki.locationalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.locationalarm.fragments.LocationAlarmListFragment;
import co.thnki.locationalarm.fragments.MapFragment;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.receivers.InternetConnectivityListener;
import co.thnki.locationalarm.services.RemoteConfigService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.ImageUtil;
import co.thnki.locationalarm.utils.LocationUtil;
import co.thnki.locationalarm.utils.PermissionUtil;

import static co.thnki.locationalarm.singletons.Otto.register;

public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener
{
    public static final String TAG = "LocationAlarmMain";
    private static final String MAP_FRAGMENT = "mapFragment";
    private static final String LOCATION_ALARM_LIST_FRAGMENT = "locationAlarmListFragment";

    @Bind(R.id.app_bar)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.mapAdView)
    RelativeLayout mMapAdView;

    AdView mAdView;

    @Bind(R.id.descriptionText)
    TextView mDescriptionText;

    @Bind(R.id.scrollUp)
    ImageView scrollUp;

    @Bind(R.id.toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindColor(R.color.colorAccent)
    int mAccentColor;

    @BindColor(R.color.my_location_radius)
    int mRadiusColor;

    @Bind(R.id.mapContainer)
    ViewGroup mMapFragmentView;

    @Bind(R.id.titleText)
    TextView titleText;
    private boolean isCollapsed;

    @Bind(R.id.titleBar)
    RelativeLayout mTitleBar;
    private MapFragment mMapFragment;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    @BindString(R.string.tapToViewList)
    String mTapToViewTheList;

    @BindString(R.string.tapToViewMap)
    String mTapToViewTheMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("LagIssue", "onCreate  : MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences mPreferences = LocationAlarmApp.getPreferences();
        ButterKnife.bind(this);
        register(this);
        //TODO set current adUnitId
        mAdView = new AdView(this);
        mAdView.setAdSize(new AdSize(ImageUtil.getAdWidth(this) - 40, 50));
        ViewGroup.LayoutParams adLayoutParams = mAdView.getLayoutParams();
        mAdView.setAdUnitId(mPreferences.getString(RemoteConfigService.AD_UNIT_ID + 1, "ca-app-pub-9949935976977846/1773589219"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(layoutParams);
        layout.addView(mAdView);
        mMapAdView.addView(layout);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback()
        {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout)
            {
                return false;
            }
        });
        params.setBehavior(behavior);
        mAppBarLayout.addOnOffsetChangedListener(this);
        FragmentManager manager = getSupportFragmentManager();
        addMapFragment(manager);
        addAlarmListFragment(manager);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
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

    private void addAlarmListFragment(FragmentManager manager)
    {
        Log.d("LagIssue", "addAlarmListFragment  : MainActivity");
        LocationAlarmListFragment fragment = new LocationAlarmListFragment();
        manager.beginTransaction()
                .replace(R.id.alarmListContainer, fragment, LOCATION_ALARM_LIST_FRAGMENT)
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
                Log.d("LagIssue", "onAdLoaded  : MainActivity");
            }

            @Override
            public void onAdFailedToLoad(int i)
            {
                super.onAdFailedToLoad(i);
                if (PermissionUtil.isConnected(MainActivity.this))
                {
                    Log.d("LagIssue", "onAdFailedToLoad  : MainActivity");
                    //loadAd();
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //setCollapsingToolBarHeight();
        loadAd();
    }

    @Override
    public void onBackPressed()
    {
        if (!isCollapsed)
        {
            mAppBarLayout.setExpanded(true, true);
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
        Otto.post(LocationAlarmListFragment.RELOAD_LIST);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
    {
        Log.d(TAG, "verticalOffset : " + verticalOffset);
        if (Math.abs(verticalOffset) == 0)
        {
            if (!isCollapsed)
            {
                Otto.post(MapFragment.DIALOG_DISMISS);
                mDescriptionText.setText(mTapToViewTheList);
                isCollapsed = true;
                scrollUp.setImageResource(R.mipmap.scroll_up);
            }
        }
        else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange())
        {
            if(isCollapsed)
            {
                mDescriptionText.setText(mTapToViewTheMap);
                scrollUp.setImageResource(R.mipmap.scroll_down);
                isCollapsed = false;
            }
        }
    }


    @OnClick(R.id.titleBar)
    public void scrollUp()
    {
        if (isCollapsed)
        {
            mAppBarLayout.setExpanded(false, true);
        }
        else
        {
            mAppBarLayout.setExpanded(true, true);
        }
    }

    @Subscribe
    public void onAlarmClicked(LocationAlarm action)
    {
        mAppBarLayout.setExpanded(true, true);
        mMapFragment.gotoLatLng(LocationUtil.getLatLng(action.latitude, action.longitude), true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    @Subscribe
    public void onInternetConnected(String action)
    {
        switch (action)
        {
            case InternetConnectivityListener.INTERNET_CONNECTED:
                loadAd();
                break;
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction()
    {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}