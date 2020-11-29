package de.smasi.tickmate.database;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class FileUtils {
    private static final String TAG = "FileUtils";
    
	/**
     * Creates the specified <code>toFile</code> as a byte for byte copy of the
     * <code>fromFile</code>. If <code>toFile</code> already exists, then it
     * will be replaced with a copy of <code>fromFile</code>. The name and path
     * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
     * <br/>
     * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
     * this function.</i>
     * 
     * @param fromFile
     *            - FileInputStream for the file to copy from.
     * @param toFile
     *            - FileInputStream for the file to copy to.
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    /**
     * Copies content of <code>fromFile</code> to <code>toStream</code> and closes both streams.
     *
     * @param fromStream - FileInputStream for the file to copy from.
     * @param toStream   - FileInputStream for the file to copy to.
     */
    public static void copyFile(InputStream fromStream, OutputStream toStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = fromStream.read(buffer);
        while (len != -1) {
            toStream.write(buffer, 0, len);
            len = fromStream.read(buffer);
        }
        fromStream.close();
        toStream.close();
    }

    public static void saveStreamToFile(InputStream fromStream, FileOutputStream toFile) throws IOException {
        byte[] buffer = new byte[fromStream.available()];
        fromStream.read(buffer);
        toFile.write(buffer);
    }

    public static class StorageInfo {

        public final String path;
        public final boolean readonly;
        public final boolean removable;
        public final int number;

        StorageInfo(String path, boolean readonly, boolean removable, int number) {
            this.path = path;
            this.readonly = readonly;
            this.removable = removable;
            this.number = number;
        }

        public String getDisplayName() {
            StringBuilder res = new StringBuilder();
            if (!removable) {
                res.append("Internal SD card");
            } else if (number > 1) {
                res.append("SD card " + number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }
            return res.toString();
        }
    }
        
        public static String getRemovableStorageDirectory() {
        	List<StorageInfo> lst = getStorageList();

        	for (StorageInfo storageInfo : lst) {
				if (!storageInfo.readonly && storageInfo.removable) {
					return storageInfo.path;
				}
			}
        	for (StorageInfo storageInfo : lst) {
				if (!storageInfo.readonly) {
					return storageInfo.path;
				}
			}
        	return Environment.getExternalStorageDirectory().getPath();
        }

        public static List<StorageInfo> getStorageList() {

            List<StorageInfo> list = new ArrayList<StorageInfo>();
            String def_path = Environment.getExternalStorageDirectory().getPath();
            boolean def_path_removable = Environment.isExternalStorageRemovable();
            String def_path_state = Environment.getExternalStorageState();
            boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                                        || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
            boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

            HashSet<String> paths = new HashSet<String>();
            int cur_removable_number = 1;

            if (def_path_available) {
                paths.add(def_path);
                list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
            }

            BufferedReader buf_reader = null;
            try {
                buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
                String line;
                Log.d(TAG, "/proc/mounts");
                while ((line = buf_reader.readLine()) != null) {
                    Log.d(TAG, line);
                    if (line.contains("vfat") || line.contains("/mnt")) {
                        StringTokenizer tokens = new StringTokenizer(line, " ");
                        String unused = tokens.nextToken(); //device
                        String mount_point = tokens.nextToken(); //mount point
                        if (paths.contains(mount_point)) {
                            continue;
                        }
                        unused = tokens.nextToken(); //file system
                        List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                        boolean readonly = flags.contains("ro");

                        if (line.contains("/dev/block/vold")) {
                            if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                                paths.add(mount_point);
                                list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
                            }
                        }
                    }
                }

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (buf_reader != null) {
                    try {
                        buf_reader.close();
                    } catch (IOException ex) {}
                }
            }
            return list;
        }
    }    
