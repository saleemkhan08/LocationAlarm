package co.thnki.locationalarm.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import co.thnki.locationalarm.R;
import co.thnki.locationalarm.interfaces.ConnectivityListener;

public class ConnectivityUtil
{

    private static final int GPS_ERROR_DIALOG_REQUEST = 1989;

    public static boolean isConnected(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                return true;
            }
        }
        return false;
    }

    public static void isConnected(final ConnectivityListener listener, final Context context)
    {
        if (!isConnected(context))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.no_network_access);
            builder.setPositiveButton("Try Again",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (isConnected(context))
                            {
                                if (listener != null)
                                {
                                    listener.onInternetConnected();
                                }
                                dialog.dismiss();
                            }
                            else
                            {
                                isConnected(listener, context);
                            }
                        }
                    });
            builder.setCancelable(false);
            builder.setTitle(R.string.internet_failure);
            builder.setOnKeyListener(new DialogInterface.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP
                            && !event.isCanceled())
                    {
                        dialog.dismiss();
                        listener.onCancelled();
                    }
                    return false;
                }
            });
            builder.show();
        }
        else
        {
            if (listener != null)
            {
                listener.onInternetConnected();
            }
        }
    }

    public static boolean isGoogleServicesOk(AppCompatActivity mActivity)
    {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (isAvailable == ConnectionResult.SUCCESS)
        {
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable))
        {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, mActivity, GPS_ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            Toast.makeText(mActivity, "Can't Connect to Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}

