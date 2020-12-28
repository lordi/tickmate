package de.smasi.tickmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by js on 10/15/15.
 */
public class TickColor {
    private static final String TAG = "TickColor";
    private String mName = "Not named";
    private int mColorValue = 0;

    private static Drawable sUnTickedButton;
    private static Drawable sTickedButton;

    // Material design colors, 400-600
    // https://www.google.com/design/spec/style/color.html
    private static int[] sColors = new int[] {
            0xf44336,
            0xec407a,
            0xab47bc,
            0x673ab7,
            0x5c6bc0,
            0x1e88e5,
            0x039be5,
            0x00bcd4,
            0x26a69a,
            0x81c784,
            0x7cb342,
            0xc0ca33,
            0xfdd835,
            0xffb300,
            0xfb8c00,
            0xf4511e,
            0xbcaaa4,
            0xbdbdbd,
            0xb0bec5,
            0x4ea6e0  // pre 1.4 tickmate color
    };
    private static List<TickColor> sTickColors;

    public TickColor(int color) {
        mColorValue = color;
        mName = Integer.toHexString(color);
    }

    public Drawable getDrawable(int alpha) {
        Log.d(TAG, "getTickedButtonDrawable " + getName());
        ColorDrawable cd = new ColorDrawable(Color.parseColor(hex()));
        cd.setAlpha(alpha);
        return cd;
    }

    public Drawable getTickedButtonDrawable(Context c) {
        Log.d(TAG, "getTickedButtonDrawable  " + getName());
        return getTickedButtonDrawable(c, mColorValue);
    }

    public static Drawable getTickedButtonDrawable(Context context, int tickButtonColor) {
        // Prepare the layers & color filter for the LayerDrawable
        ColorFilter cf = new LightingColorFilter(tickButtonColor, 0);
        //ColorDrawable buttonCenterDrawable = new ColorDrawable(0xFF000000 + tickButtonColor);
        Drawable buttonCenterDrawable = ContextCompat.getDrawable(context, R.drawable.mask_64);
        Drawable buttonBorderDrawable = ContextCompat.getDrawable(context, R.drawable.on_64);
        buttonCenterDrawable.setColorFilter(cf);
        sTickedButton = new LayerDrawable(new Drawable[]{buttonCenterDrawable, buttonBorderDrawable});
        return sTickedButton;
    }

    public static Drawable getUnTickedButtonDrawable(Context context) {
        if (sUnTickedButton == null) {
            sUnTickedButton = ContextCompat.getDrawable(context, R.drawable.off_64);
        }
        return sUnTickedButton;
    }

    public String hex() {
        return String.format("#%06X", mColorValue);
    }

    public String getName() {
        return hex();
    }

    public int getColorValue() {
        return mColorValue;
    }

    // TODO rename this
    // Give the drawables used for the preference dialog
    public static Drawable getPreferenceDrawable(Context c, int index) {
        return getTickedButtonDrawable(c, sColors[index]);
    }

    public static TickColor getColor(int index) {
        if (sTickColors == null) {
            sTickColors = new ArrayList();
            for (int i = 0; i < sColors.length; i++) {
                sTickColors.add(new TickColor(sColors[i]));
            }
        }
        return sTickColors.get(index);
    }

    public static int getNumberOfColors() {
        return sColors.length;
    }

    public void setColorValue(int colorValue) {
        mColorValue = colorValue;
    }

}


