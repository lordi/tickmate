package de.smasi.tickmate;

import android.app.Application;

public class Globals extends Application {
    /**
     * Created by js on 8/20/15.
     */

    private static Globals mContext;

    public Globals() {
        mContext = this;
    }

    public static Globals getInstance() {
        if (mContext == null) {
            mContext = new Globals();
        }
        return mContext;
    }
}