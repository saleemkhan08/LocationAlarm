package co.thnki.locationalarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class LoadingActivity extends AppCompatActivity
{
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 198;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("LagIssue", "onCreate : loading");
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_loading);
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(LocationAlarmApp.getTypeFace());
        checkGooglePlayServices();
    }

    private void launchMainActivity()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d("LagIssue", "Launching MainActivity : loading");
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(LoadingActivity.this, null);
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        }, 500);
    }

    private void checkGooglePlayServices()
    {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(this);
        if (code == ConnectionResult.SUCCESS)
        {
            onActivityResult(REQUEST_GOOGLE_PLAY_SERVICES, Activity.RESULT_OK, null);
        }
        else if (api.isUserResolvableError(code))
        {
            api.showErrorDialogFragment(this, code, REQUEST_GOOGLE_PLAY_SERVICES);
        }
        else
        {
            Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK)
                {
                    launchMainActivity();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}