package de.smasi.tickmatedata.wear;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;
import java.util.LinkedHashMap;

import de.smasi.tickmatedata.models.Track;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class WearDataClient extends DataClient {

    public static final String WEAR_MESSAGE_GET_TRACKS = "/get_tracks";
    public static final String WEAR_MESSAGE_IS_TICKED = "/is_ticked";
    public static final String WEAR_MESSAGE_GET_TICKS = "/get_ticks";
    public static final String WEAR_MESSAGE_SET_TICK = "/set_tick";
    public static final String WEAR_MESSAGE_REMOVE_TICK = "/remove_tick";
    public static final String WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY = "/remove_last_tick_of_day";

    public WearDataClient(Context context, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        super(context, connectionCallbacks, connectionFailedListener);
    }

    public void getTracks() {
        sendMessageToAllNodes(WearDataClient.WEAR_MESSAGE_GET_TRACKS, null);
    }

    public void getTickCountForDay(final Track track, final Calendar calendar) {
        LinkedHashMap<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("track", track);
        bundle.put("calendar", calendar);
        byte[] data = DataUtils.dataFromHashMap(bundle);
        sendMessageToAllNodes(WearDataClient.WEAR_MESSAGE_GET_TICKS, data);
    }

    public void isTicked(Track t, Calendar date, Boolean hasTimeInfo) {
        LinkedHashMap<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("track", t);
        bundle.put("calendar", date);
        bundle.put("hasTimeInfo", hasTimeInfo);
        byte[] data = DataUtils.dataFromHashMap(bundle);
        sendMessageToAllNodes(WearDataClient.WEAR_MESSAGE_IS_TICKED, data);
    }

    public void setTick(final Track track, final Calendar calendar, final Boolean hasTimeInfo) {
        LinkedHashMap<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("track", track);
        bundle.put("calendar", calendar);
        bundle.put("hasTimeInfo", hasTimeInfo);
        byte[] data = DataUtils.dataFromHashMap(bundle);
        sendMessageToAllNodes(WearDataClient.WEAR_MESSAGE_SET_TICK, data);
    }

    public void removeLastTickOfDay(Track track, Calendar calendar) {
        LinkedHashMap<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("track", track);
        bundle.put("calendar", calendar);
        byte[] data = DataUtils.dataFromHashMap(bundle);
        sendMessageToAllNodes(WearDataClient.WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY, data);
    }

    public void removeTick(Track track, Calendar date, final Boolean hasTimeInfo) {
        LinkedHashMap<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("track", track);
        bundle.put("calendar", date);
        bundle.put("hasTimeInfo", hasTimeInfo);
        byte[] data = DataUtils.dataFromHashMap(bundle);
        sendMessageToAllNodes(WearDataClient.WEAR_MESSAGE_REMOVE_TICK, data);
    }
}
