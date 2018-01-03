package jp.co.rjc.util;

import android.content.Context;

/**
 * Created by anonymous on 2017/12/30.
 */

final public class LayoutUtils {

    public static LayoutUtils sInstance;
    private static Context sContext;

    private LayoutUtils(final Context context){
        sContext = context;
    }

    public static LayoutUtils getsInstance(final Context context) {
        if  (sInstance == null) {
            sInstance = new LayoutUtils(context);
        }
        return sInstance;
    }

    public int getDpToPx(int dp){
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
