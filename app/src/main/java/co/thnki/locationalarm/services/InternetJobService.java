package co.thnki.locationalarm.services;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class InternetJobService extends JobService
{
    public static final String JOB_SERVICE = "jobService";
    @Override
    public boolean onStartJob(JobParameters jobParameters)
    {
        Log.d("LagIssue", "onStartJob : "+jobParameters.getJobId());
        Intent intent = new Intent(this, RemoteConfigService.class);
        //intent.putExtra(JOB_SERVICE, this);
        startService(intent);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters)
    {
        Log.d("LagIssue", "onStopJob : "+jobParameters.getJobId());
        return true;
    }
}
