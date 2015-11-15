package de.smasi.tickmatedata.wear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.smasi.tickmatedata.models.Track;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class DataUtils {

    public static List<Track> getTrackListFromData (byte[] data) {
        List<Track> tracks = new ArrayList<>();
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            try {
                tracks = (List<Track>) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    ois.close();
                } catch (IOException e) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tracks;
    }

    public static byte[] dataFromTrackList(List<Track> objectList) {
        byte [] data = new byte[] {};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(objectList);
            data = baos.toByteArray();
        } catch (IOException e) {

        } finally {
            try {
                baos.close();
            } catch (IOException e) {

            }
        }
        return data;
    }
}
