package co.thnki.locationalarm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class LoadingActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("LagIssue", "onCreate : loading");
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_loading);
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(LocationAlarmApp.getTypeFace());
        launchMainActivity();
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
        }, 1000);
    }
}