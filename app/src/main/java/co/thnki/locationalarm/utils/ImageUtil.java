package co.thnki.locationalarm.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ImageUtil
{
    public static int dp(Context mContext, double value)
    {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        double dp = (float) value;
        double fpixels = metrics.density * dp;
        return (int) fpixels;
    }
}
