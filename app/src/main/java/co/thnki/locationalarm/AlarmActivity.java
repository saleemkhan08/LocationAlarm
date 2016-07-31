package co.thnki.locationalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.receivers.InternetConnectivityListener;
import co.thnki.locationalarm.services.AlarmAudioService;
import co.thnki.locationalarm.services.RemoteConfigService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.LocationUtil;
import co.thnki.locationalarm.view.RippleBackground;

import static android.R.attr.action;

public class AlarmActivity extends AppCompatActivity
{

    @Bind(R.id.content)
    RippleBackground rippleBackground;

    @Bind(R.id.alarmAdView)
    RelativeLayout mAlarmAdView;
    private NativeExpressAdView mNativeAdView;

    @Bind(R.id.card_view)
    CardView nativeAdViewWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_acivity);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        SharedPreferences mPreferences = LocationAlarmApp.getPreferences();


        TextView message = (TextView) findViewById(R.id.message);
        TextView youHaveReached = (TextView) findViewById(R.id.youHaveReached);

        Typeface face = LocationAlarmApp.getTypeFace();
        youHaveReached.setTypeface(face);
        message.setTypeface(face);

        LocationAlarm alarm = getIntent().getParcelableExtra(LocationAlarm.ALARM);
        message.setText(LocationUtil.getAddressLines(alarm.address, 3));
        //TODO update the current AdUnitId
        mNativeAdView = new NativeExpressAdView(this);

        mNativeAdView.setAdSize(new AdSize(320,150));
        mNativeAdView.setAdUnitId(mPreferences.getString(RemoteConfigService.AD_UNIT_ID+"3","ca-app-pub-9949935976977846/4727055612"));
        mAlarmAdView.addView(mNativeAdView);

        loadAd();
    }

    private void loadAd()
    {
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("51B143E236817102C0BC44F96EE8A5F7")
                .build();
        mNativeAdView.loadAd(request);
        mNativeAdView.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                Log.d("alarmAdapter", "onAdLoaded");
                nativeAdViewWrapper.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        rippleBackground.startRippleAnimation();
    }

    @OnClick(R.id.stopAlarm)
    public void stop()
    {
        rippleBackground.stopRippleAnimation();
        startActivity(new Intent(this, MainActivity.class));
        Otto.post(AlarmAudioService.STOP_ALARM_AUDIO);
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        rippleBackground.stopRippleAnimation();
        Otto.post(AlarmAudioService.STOP_ALARM_AUDIO);
        Otto.post(MainActivity.RELOAD_LIST);

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        rippleBackground.stopRippleAnimation();
        Otto.post(AlarmAudioService.STOP_ALARM_AUDIO);
        Otto.post(MainActivity.RELOAD_LIST);
    }

    @Subscribe
    public void onInternetConnectivityChange(String status)
    {
        Log.d("ConnectivityListener", "onInternetConnected : Alarm activity : " + action );
        switch (status)
        {
            case InternetConnectivityListener.INTERNET_CONNECTED :
                loadAd();
                break;
        }
    }
}
