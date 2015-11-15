package de.smasi.tickmatedata.wear;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class DataClient {

    protected Context context;

    public static final String WEAR_MESSAGE_GET_TRACKS = "/get_tracks";
    public static final String WEAR_MESSAGE_GET_TICKS = "/get_ticks";

    public GoogleApiClient googleApiClient;

    public DataClient(Context context) {
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                })
                .build();
        googleApiClient.connect();
    }

    public void sendMessage(final String messagePath, final byte[] payload, final Node node) {
        List<Node> nodes = new ArrayList<Node>() {{ add(node); }};
        sendMessage(messagePath, payload, nodes);
    }

    public void sendMessage(final String messagePath, final byte[] payload, final List<Node> nodes) {
        for (final Node node: nodes) {
            Wearable.MessageApi
                    .sendMessage(googleApiClient, node.getId(), messagePath, payload)
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.v("DataClient", "Did send message to " + node.getDisplayName());
                        }
                    });
        }
    }
}
