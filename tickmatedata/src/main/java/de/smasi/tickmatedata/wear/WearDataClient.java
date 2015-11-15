package de.smasi.tickmatedata.wear;

import android.content.Context;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class WearDataClient extends DataClient {

    public WearDataClient(Context context) {
        super(context);
    }

    public void getTracks() {

        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node: getConnectedNodesResult.getNodes()) {
                    sendMessage(DataClient.WEAR_MESSAGE_GET_TRACKS, null, node);
                }
            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // get node
//                NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
//                for (Node node: nodesResult.getNodes()) {
//                    sendMessage(DataClient.WEAR_MESSAGE_GET_TRACKS, null, node);
//                }
//            }
//        });
    }
}
