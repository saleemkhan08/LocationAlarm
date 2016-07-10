package co.thnki.locationalarm.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.squareup.otto.Subscribe;

import co.thnki.locationalarm.R;
import co.thnki.locationalarm.singletons.Otto;

public class AlarmAudioService extends Service implements AudioManager.OnAudioFocusChangeListener
{
    public static final String STOP_ALARM_AUDIO = "stopAlarmAudio";

    public AlarmAudioService()
    {
    }

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("AlarmAudioService", "started");
        Otto.register(this);
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Request audio focus for playback
        int result = am.requestAudioFocus(this,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        Log.d("AlarmAudioService", "result : " + result +" = "+AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        {
            Log.d("AlarmAudioService", "Granted");

        }

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
        mediaPlayer.setLooping(true);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 200, 975};
        vibrator.vibrate(pattern, 0);
        Log.d("AlarmAudioService", "Granted");

        return START_NOT_STICKY;
    }

    @Override
    public void onAudioFocusChange(int i)
    {

    }

    @Subscribe
    public void stopAlarmAudio(String action)
    {
        Log.d("AlarmAudioService", "stopAlarmAudio");
        mediaPlayer.stop();
        vibrator.cancel();
        stopSelf();
    }

    @Override
    public void onDestroy()
    {
        Log.d("AlarmAudioService", "onDestroy");
        super.onDestroy();
    }
}
