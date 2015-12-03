package de.smasi.tickmatedata.wear;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class DataClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = null;
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = null;
    protected Context context;

    public GoogleApiClient googleApiClient;

    public DataClient(Context context, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        this.context = context;
        this.connectionCallbacks = connectionCallbacks;
        this.connectionFailedListener = connectionFailedListener;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (connectionCallbacks != null) {
            connectionCallbacks.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (connectionCallbacks != null) {
            connectionCallbacks.onConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionFailedListener != null) {
            connectionFailedListener.onConnectionFailed(connectionResult);
        }
    }

    public void sendMessage(final String messagePath, final byte[] payload, final Node node) {
        List<Node> nodes = new ArrayList<Node>() {{ add(node); }};
        sendMessage(messagePath, payload, nodes);
    }

    public void sendMessageToRemoteNodes(final String messagePath, final byte[] payload) {
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                sendMessage(messagePath, payload, nodes);
            }
        });
    }

    public void sendMessage(final String messagePath, final byte[] payload, final List<Node> nodes) {
        for (final Node node: nodes) {
            Wearable.MessageApi
                    .sendMessage(googleApiClient, node.getId(), messagePath, payload)
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.v("DataClient", "Did send " + messagePath + " to " + node.getDisplayName());
                        }
                    });
        }
    }
}
