package de.smasi.tickmate;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import de.smasi.tickmatedata.database.DataSource;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataClient;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.HandsetDataClient;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class HandsetDataService extends WearableListenerService {

    private DataSource dataSource = DataSource.getInstance();
    private HandsetDataClient dataClient = null;

    private HandsetDataClient getDataClient() {
        if (dataClient == null) {
            dataClient = new HandsetDataClient(this);
        }
        return dataClient;
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        if ( messageEvent.getPath().equalsIgnoreCase( DataClient.WEAR_MESSAGE_GET_TRACKS ) ) {
            final List<Track> tracks = dataSource.getTracks();
            final byte[] data = DataUtils.dataFromTrackList(tracks);

            final HandsetDataClient dataClient = getDataClient();
            // get nodes
            Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    for (Node node: getConnectedNodesResult.getNodes()) {
                        dataClient.sendMessage(DataClient.WEAR_MESSAGE_GET_TRACKS, data, node);
                    }
                }
            });
        } else if (messageEvent.getPath().equalsIgnoreCase( DataClient.WEAR_MESSAGE_GET_TICKS )) {
//            final List<Tick> ticks = dataSource.getTicksForDay();
//            final byte[] data = dataFromObject(ticks);
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    // get node
//                    NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
//                    for (Node node: nodesResult.getNodes()) {
//                        if (node.getId().equals(messageEvent.getSourceNodeId())) {
//                            sendMessage(WearDataService.WEAR_MESSAGE_GET_TRACKS, data, node);
//                        }
//                    }
//                }
//            });
        } else {
            super.onMessageReceived(messageEvent);
        }
    }


}
