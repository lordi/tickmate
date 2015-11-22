package de.smasi.tickmate;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import de.smasi.tickmatedata.wear.WearDataClient;


/**
 * Created by Adrian Geuss on 14.11.15.
 */
public class WearDataService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if ( messageEvent.getPath().equalsIgnoreCase( WearDataClient.WEAR_MESSAGE_GET_TRACKS ) ||
                messageEvent.getPath().equalsIgnoreCase( WearDataClient.WEAR_MESSAGE_GET_TICKS )) {
//            Intent intent = new Intent( this, WearMainActivity.class );
//            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
//            startActivity( intent );
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
