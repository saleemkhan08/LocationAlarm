package co.thnki.locationalarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.services.AlarmAudioService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.view.RippleBackground;

public class AlarmActivity extends AppCompatActivity
{
    @Bind(R.id.content)
    RippleBackground rippleBackground;

    @Bind(R.id.nativeAdView)
    NativeExpressAdView nativeAdView;

    @Bind(R.id.card_view)
    CardView nativeAdViewWrapper;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_acivity);
        ButterKnife.bind(this);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AlarmActivity");
        mWakeLock.acquire();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        TextView message = (TextView) findViewById(R.id.message);
        TextView youHaveReached = (TextView) findViewById(R.id.youHaveReached);

        Typeface face = Typeface.createFromAsset(getAssets(),
                "Gabriola.ttf");
        youHaveReached.setTypeface(face);
        message.setTypeface(face);

        final LocationAlarm alarm = getIntent().getParcelableExtra(LocationAlarm.ALARM);
        message.setText(getAddressLines(alarm.address, 3));

        AdRequest request = new AdRequest.Builder().addTestDevice("51B143E236817102C0BC44F96EE8A5F7").build();
        nativeAdView.loadAd(request);
        nativeAdView.setAdListener(new AdListener()
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

    @Override
    protected void onStop()
    {
        super.onStop();
        stop();
    }

    @OnClick(R.id.stopAlarm)
    public void stop()
    {
        rippleBackground.stopRippleAnimation();
        startActivity(new Intent(AlarmActivity.this, MainActivity.class));
        Otto.post(AlarmAudioService.STOP_ALARM_AUDIO);
        if (mWakeLock.isHeld())
        {
            mWakeLock.release();
        }

        finish();
    }

    public static String getAddressLines(String address, int noOfLines)
    {
        String[] addressLines = address.split(",");
        String msg = "";
        int len = addressLines.length;
        for (int i = 0; i < len; i++)
        {
            if (i < (noOfLines - 1))
            {
                msg += addressLines[i] + ", ";
            }
        }
        return msg.substring(0, msg.length() - 2);
    }
}
