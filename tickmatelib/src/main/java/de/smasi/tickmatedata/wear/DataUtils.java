package de.smasi.tickmatedata.wear;

import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import de.smasi.tickmatedata.models.Tick;
import de.smasi.tickmatedata.models.Track;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class DataUtils {

    public static <T> T getObjectFromData (byte[] data) {
        T object = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            try {
                object = readObject(ois);
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

        return object;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObject(
            ObjectInputStream in
    ) throws IOException, ClassNotFoundException {
        return (T)in.readObject();
    }

    public static byte[] dataFromBundle(Bundle bundle) {
        byte [] data = new byte[] {};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(bundle);
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

    public static byte[] dataFromTickList(List<Tick> objectList) {
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
