package co.thnki.locationalarm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.locationalarm.fragments.LocationAlarmListFragment;
import co.thnki.locationalarm.fragments.MapFragment;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.receivers.InternetConnectivityListener;
import co.thnki.locationalarm.singletons.Otto;
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
    AdView mMapAdView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21)
        {
            //TransitionInflater inflater = TransitionInflater.from(LoginActivity.this);
            //Transition transition = inflater.inflateTransition(R.transition.login_to_main_transition);
            Slide slide = new Slide();
            slide.setDuration(700);
            slide.setSlideEdge(Gravity.RIGHT);
            getWindow().setEnterTransition(slide);
        }
        setContentView(R.layout.activity_main);


        ButterKnife.bind(this);
        register(this);
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
        addAlarmListFragment(manager);
        addMapFragment(manager);
    }

    private void addMapFragment(FragmentManager manager)
    {
        mMapFragment = new MapFragment();
        manager.beginTransaction()
                .replace(R.id.mapContainer, mMapFragment, MAP_FRAGMENT)
                .commit();
    }

    private void addAlarmListFragment(FragmentManager manager)
    {
        LocationAlarmListFragment fragment = new LocationAlarmListFragment();
        manager.beginTransaction()
                .replace(R.id.alarmListContainer, fragment, LOCATION_ALARM_LIST_FRAGMENT)
                .commit();
    }

    private void loadAd()
    {
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mMapAdView.loadAd(adRequest);
        mMapAdView.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                TransitionManager.beginDelayedTransition(mMapAdView);
                mMapAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int i)
            {
                super.onAdFailedToLoad(i);
                if (PermissionUtil.isConnected(MainActivity.this))
                {
                    loadAd();
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
        setCollapsingToolBarHeight();
    }

    private void setCollapsingToolBarHeight()
    {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        layoutParams.height = getResources().getDisplayMetrics().heightPixels - mTitleBar.getLayoutParams().height;

        TransitionManager.beginDelayedTransition(mAppBarLayout);
        mAppBarLayout.setLayoutParams(layoutParams);
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
            }
            isCollapsed = true;
            scrollUp.setImageResource(R.mipmap.scroll_up);
            mDescriptionText.setText(getText(R.string.tapToViewList));
        }
        else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange())
        {
            isCollapsed = false;
            scrollUp.setImageResource(R.mipmap.scroll_down);
            mDescriptionText.setText(getText(R.string.tapToViewMap));

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
}