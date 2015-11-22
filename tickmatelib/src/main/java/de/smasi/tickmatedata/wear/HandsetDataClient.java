package de.smasi.tickmatedata.wear;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class HandsetDataClient extends DataClient {

    public HandsetDataClient(Context context, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        super(context, connectionCallbacks, connectionFailedListener);
    }
}
