package de.smasi.tickmate;

import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Calendar;
import java.util.List;

import de.smasi.tickmatedata.database.DataSource;
import de.smasi.tickmatedata.models.Tick;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.HandsetDataClient;
import de.smasi.tickmatedata.wear.WearDataClient;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class HandsetDataService extends WearableListenerService {

    private DataSource dataSource = DataSource.getInstance();
    private HandsetDataClient dataClient = null;

    private HandsetDataClient getDataClient() {
        if (dataClient == null) {
            dataClient = new HandsetDataClient(this, null, null);
        }
        return dataClient;
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

        if ( messageEvent.getPath().equalsIgnoreCase( WearDataClient.WEAR_MESSAGE_GET_TRACKS ) ) {
            final List<Track> tracks = dataSource.getTracks();
            final byte[] data = DataUtils.dataFromTrackList(tracks);

            final HandsetDataClient dataClient = getDataClient();
            // get nodes
            Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    for (Node node: getConnectedNodesResult.getNodes()) {
                        dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_GET_TRACKS, data, node);
                    }
                }
            });

        } else if (messageEvent.getPath().equalsIgnoreCase( WearDataClient.WEAR_MESSAGE_GET_TICKS )) {
            Bundle args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.getSerializable("track");
                Calendar calendar = (Calendar) args.getSerializable("calendar");
                List<Tick> ticks = dataSource.getTicksForDay(track, calendar);
                byte[] tickData = DataUtils.dataFromTickList(ticks);
                Bundle response = new Bundle();
                response.putByteArray("ticks", tickData);
                response.putSerializable("track", track);
                response.putSerializable("calendar", calendar);
                final byte[] data = DataUtils.dataFromBundle(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node: getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_GET_TICKS, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_IS_TICKED)) {
            Bundle args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.getSerializable("track");
                Calendar calendar = (Calendar) args.getSerializable("calendar");
                boolean hasTimeInfo = args.getBoolean("hasTimeInfo");

                boolean isTicked = dataSource.isTicked(track, calendar, hasTimeInfo);

                Bundle response = new Bundle();
                response.putSerializable("track", track);
                response.putSerializable("calendar", calendar);
                response.putBoolean("isTicked", isTicked);
                final byte[] data = DataUtils.dataFromBundle(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_IS_TICKED, data, node);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_SET_TICK)) {
            Bundle args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.getSerializable("track");
                Calendar calendar = (Calendar) args.getSerializable("calendar");
                boolean hasTimeInfo = args.getBoolean("hasTimeInfo");

                dataSource.setTick(track, calendar, hasTimeInfo);

                Bundle response = new Bundle();
                response.putSerializable("track", track);
                response.putSerializable("calendar", calendar);
                if (!track.multipleEntriesEnabled()) {
                    response.putBoolean("isTicked", dataSource.isTicked(track, calendar, hasTimeInfo));
                } else {
                    List<Tick> ticks = dataSource.getTicksForDay(track, calendar);
                    byte[] tickData = DataUtils.dataFromTickList(ticks);
                    response.putByteArray("ticks", tickData);
                }
                final byte[] data = DataUtils.dataFromBundle(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_SET_TICK, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY)) {
            Bundle args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.getSerializable("track");
                Calendar calendar = (Calendar) args.getSerializable("calendar");

                dataSource.removeLastTickOfDay(track, calendar);
                List<Tick> ticks = dataSource.getTicksForDay(track, calendar);
                byte[] tickData = DataUtils.dataFromTickList(ticks);

                Bundle response = new Bundle();
                response.putSerializable("track", track);
                response.putSerializable("calendar", calendar);
                response.putByteArray("ticks", tickData);

                final byte[] data = DataUtils.dataFromBundle(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_REMOVE_TICK)) {
            Bundle args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.getSerializable("track");
                Calendar calendar = (Calendar) args.getSerializable("calendar");
                boolean hasTimeInfo = args.getBoolean("hasTimeInfo");

                dataSource.removeTick(track, calendar);

                Bundle response = new Bundle();
                response.putSerializable("track", track);
                response.putSerializable("calendar", calendar);
                if (!track.multipleEntriesEnabled()) {
                    response.putBoolean("isTicked", dataSource.isTicked(track, calendar, hasTimeInfo));
                }
                final byte[] data = DataUtils.dataFromBundle(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_REMOVE_TICK, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
