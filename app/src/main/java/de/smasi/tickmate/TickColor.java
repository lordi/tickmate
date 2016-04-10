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
    private static int[] sColors = new int[]{  // TODO choose final colors and move to xml
            0xe57373, 0xef5350, 0xe53935, 0xe53935, 0xc62828,
            0xc62828, 0xff8a80, 0xf06292, 0xec407a, 0xd81b60,
            0xc2185b, 0xad1457, 0xad1457, 0x880e4f, 0xba68c8,
            0xab47bc, 0x8e24aa, 0x7b1fa2, 0x6a1b9a, 0x6a1b9a,
            0x4a148c, 0x9575cd, 0x7e57c2, 0x5e35b1, 0x512da8,
            0x4527a0, 0x4527a0, 0x311b92, 0x7986cb, 0x5c6bc0,
            0x3949ab, 0x303f9f, 0x283593, 0x283593, 0x8c9eff,
            0x64b5f6, 0x2196f3, 0x1e88e5, 0x1976d2, 0x1565c0,
            0x1565c0, 0x0d47a1, 0x4fc3f7, 0x03a9f4, 0x039be5,
            0x039be5, 0x0277bd, 0x0277bd, 0x80d8ff, 0x4dd0e1,
            0x00bcd4, 0x00acc1, 0x0097a7, 0x00838f, 0x00838f,
            0x84ffff, 0x4db6ac, 0x009688, 0x00897b, 0x00796b,
            0x00695c, 0x00695c, 0x004d40, 0x66bb6a, 0x4caf50,
            0x388e3c, 0x2e7d32, 0x2e7d32, 0x1b5e20, 0x81c784,
            0xaed581, 0x8bc34a, 0x7cb342, 0x689f38, 0x689f38,
            0x558b2f, 0x33691e, 0xdce775, 0xd4e157, 0xc0ca33,
            0xafb42b, 0xafb42b, 0x9e9d24, 0xf4ff81, 0xaeea00,
            0x64dd17, 0xfff176, 0xffeb3b, 0xfdd835, 0xfbc02d,
            0xfbc02d, 0xf9a825, 0xffff8d, 0xffd600, 0xffd54f,
            0xffc107, 0xffb300, 0xffb300, 0xff8f00, 0xff8f00,
            0xffe57f, 0xffb74d, 0xff9800, 0xfb8c00, 0xf57c00,
            0xef6c00, 0xef6c00, 0xff9100, 0xff8a65, 0xff7043,
            0xff5722


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


