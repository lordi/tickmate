package de.smasi.tickmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

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

    // Material design colors, 600
    // https://www.google.com/design/spec/style/color.html
    private static int[] sColors = new int[] {
            0xe53935,
            0xd81b60,
            0x8e24aa,
            0x5e35b1,
            0x3949ab,
            0x1e88e5,
            0x039be5,
            0x00acc1,
            0x00897b,
            0x43a047,
            0x7cb342,
            0xc0ca33,
            0xfdd835,
            0xffb300,
            0xfb8c00,
            0xf4511e,
            0x6d4c41,
            0x757575,
            0x546e7a,
    };
    private static List<TickColor> sTickColors;

    public TickColor(int color) {
        Log.d(TAG, "TickColor: " + Integer.toHexString(color));
        mColorValue = color;
        mName = Integer.toHexString(color);
    }

    public Drawable getDrawable(int alpha) {
        ColorDrawable cd = new ColorDrawable(Color.parseColor(hex()));
        cd.setAlpha(alpha);
        return cd;
    }

    public Drawable getTickedButtonDrawable(Context c) {
        Log.d(TAG, "getTickedButtonDrawable with mColorValue = " + Integer.toHexString(mColorValue));
        return getTickedButtonDrawable(c, mColorValue);
    }

    public static Drawable getTickedButtonDrawable(Context context, int tickButtonColor) {
        // Prepare the layers & color filter for the LayerDrawable
        ColorFilter cf = new LightingColorFilter(0xFFFFFF, tickButtonColor);

        Drawable buttonCenterDrawable = context.getDrawable(R.drawable.tick_button_center_no_frame_64);
        Drawable buttonBorderDrawable = context.getDrawable(R.drawable.tick_button_frame_64);
        buttonCenterDrawable.setColorFilter(cf);
        sTickedButton = new LayerDrawable(new Drawable[]{buttonCenterDrawable, buttonBorderDrawable});
        return sTickedButton;
    }

    public static Drawable getUnTickedButtonDrawable(Context context) {
        if (sUnTickedButton == null) {
            sUnTickedButton = context.getDrawable(R.drawable.off_64);
        }
        return sUnTickedButton;
    }

    public String hex() {
        return "#" + Integer.toHexString(mColorValue);
    }

    public String getName() {
        return Integer.toHexString(mColorValue);
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


