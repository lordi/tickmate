package de.smasi.tickmate.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import de.smasi.tickmatedata.models.Track;

/**
 * Created by Adrian Geuss on 24.11.15.
 */
public class TrackUsage {

    private static String getTrackUsageId(Track track) {
        return "trackUsage-" + track.getId();
    }

    public static List<Track> sortTracksByUsage(Context context, List<Track> tracks) {
        final LinkedHashMap<Integer, Date> trackUsage = new LinkedHashMap<>();
        for (Track track: tracks) {
            String usageId = getTrackUsageId(track);
            Long usage = PreferenceManager.getDefaultSharedPreferences(context).getLong(usageId, 0);
            trackUsage.put(track.getId(), new Date(usage));
        }

        Collections.sort(tracks, new Comparator<Track>() {
            @Override
            public int compare(Track lhs, Track rhs) {
                Date lhsUsageDate = trackUsage.get(lhs.getId());
                Date rhsUsageDate = trackUsage.get(rhs.getId());
                return rhsUsageDate.compareTo(lhsUsageDate);
            }
        });

        return tracks;
    }

    public static void usedTrack(Context context, Track track) {
        String usageId = getTrackUsageId(track);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(usageId, new Date().getTime()).apply();
    }
}
