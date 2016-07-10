package co.thnki.locationalarm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class LoadingActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= 21)
        {
            //TransitionInflater inflater = TransitionInflater.from(LoginActivity.this);
            //Transition transition = inflater.inflateTransition(R.transition.login_to_main_transition);
            Slide fade = new Slide();
            fade.setDuration(700);
            fade.setSlideEdge(Gravity.START);
            getWindow().setEnterTransition(fade);
            getWindow().setExitTransition(fade);
        }
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
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(LoadingActivity.this, null);
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        }, 1000);
        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                finish();
            }
        }, 3000);
    }
}