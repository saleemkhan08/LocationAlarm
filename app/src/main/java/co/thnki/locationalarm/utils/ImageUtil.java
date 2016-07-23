package co.thnki.locationalarm.utils;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

public class ImageUtil
{
    public static int pixels(Context mContext, double dp)
    {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        double pixels = metrics.density * dp;
        return (int) pixels;
    }

    public static int dp(Context mContext, double pixels)
    {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        double dp = pixels/metrics.density;
        return (int) dp;
    }

    public static int getAdWidth(AppCompatActivity context)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthInPix = metrics.widthPixels;
        int width = dp(context, widthInPix);
        if(width < 1200)
        {
            return width;
        }
        return 1200;
    }
}
