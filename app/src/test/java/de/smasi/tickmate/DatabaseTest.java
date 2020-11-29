package de.smasi.tickmate;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.models.Group;
import de.smasi.tickmate.models.Track;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@Config(sdk = Build.VERSION_CODES.O)
@RunWith(RobolectricTestRunner.class)
public class DatabaseTest {
    ActivityController<Tickmate> activityController;
    Tickmate tickmate;
    DataSource dataSource;

    Method openMethod, closeMethod;

    @Before
    public void setUp() throws NoSuchMethodException {

        activityController = Robolectric.buildActivity(Tickmate.class).create().start().pause();
        tickmate = activityController.get();
        dataSource = DataSource.getInstance();

        // set TracksDataSource open method accessible
        openMethod = dataSource.getClass().getDeclaredMethod("open");
        openMethod.setAccessible(true);

        // set TracksDataSource close method accessible
        closeMethod = dataSource.getClass().getDeclaredMethod("close");
        closeMethod.setAccessible(true);
    }

    @After
    public void tearDown() {
        Field databseOpenHelperInstance, tracksDataSourceInstance;

        try {
            activityController.stop().destroy();

            databseOpenHelperInstance = DatabaseOpenHelper.class.getDeclaredField("sharedInstance");
            databseOpenHelperInstance.setAccessible(true);
            databseOpenHelperInstance.set(null, null);

            tracksDataSourceInstance = DataSource.class.getDeclaredField("mInstance");
            tracksDataSourceInstance.setAccessible(true);
            tracksDataSourceInstance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void databaseCreateExportImport() throws Exception {
        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(0));
        closeMethod.invoke(dataSource);

        Track t = new Track("Testing", "Run my tests");
        t.setEnabled(true);
        assertThat(t.isEnabled(), is(true));
        assertThat(t.isSectionHeader(), is(false));

        openMethod.invoke(dataSource);
        dataSource.storeTrack(t);
        closeMethod.invoke(dataSource);

        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(1));
        closeMethod.invoke(dataSource);

        ByteArrayOutputStream exportedDb = new ByteArrayOutputStream();
        DatabaseOpenHelper db = DatabaseOpenHelper.getInstance(tickmate);
        db.exportDatabase(exportedDb);

        Track t2 = new Track("Testing 2", "Run my tests again");
        openMethod.invoke(dataSource);
        dataSource.storeTrack(t2);
        closeMethod.invoke(dataSource);

        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(2));
        closeMethod.invoke(dataSource);

        ByteArrayInputStream importDb = new ByteArrayInputStream(exportedDb.toByteArray());
        db.importDatabase(importDb);

        // reimported previous database, so track count should be one again:
        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(1));
        closeMethod.invoke(dataSource);
    }

    @Test
    public void tickmateShouldAppearInDescription() throws Exception {
        String hello = tickmate.getResources().getString(
                R.string.about_description);
        assertThat(hello, containsString("Tickmate"));

    }

    @Test
    public void trackDefaultIconResolvesToResID() throws Exception {
        Track t = new Track("Test track", "Cats and dogs");
        assertThat(t.getDescription(), is("Cats and dogs"));
        assertThat(t.getIcon(), not(is("")));
        assertThat(t.getIcon(), not(nullValue()));
        assert (t.getIconId(RuntimeEnvironment.application.getApplicationContext()) > 0);
    }

    @Test
    public void legacyDatabaseVersion10ShouldBeImportable() throws Exception {
        InputStream is = tickmate.getAssets().open("test/smiley-version10.db");
        File intDb = tickmate.getApplicationContext().getDatabasePath("tickmate.db");
        intDb.getParentFile().mkdirs();
        DatabaseOpenHelper db = DatabaseOpenHelper.getInstance(tickmate);
        db.importDatabase(is);

        // the legacy db should have 8 tracks (6 active)
        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(8));
        assertThat(dataSource.getActiveTracks().size(), is(6));
        assertThat(dataSource.getTickCount(1), is(28));
        assertThat(dataSource.getTickCount(2), is(2));
        assertThat(dataSource.getTickCount(3), is(13));
        // make sure that no groups have been imported with a version 10 database
        // (groups did not exist back then)
        assertThat(dataSource.getGroups().size(), is(0));
        closeMethod.invoke(dataSource);
        ;
    }

    @Test
    public void legacyDatabaseVersion12ShouldBeImportable() throws Exception {
        InputStream is = tickmate.getAssets().open("test/tickmate-version12.db");
        DatabaseOpenHelper db = DatabaseOpenHelper.getInstance(tickmate);
        File intDb = tickmate.getApplicationContext().getDatabasePath("tickmate.db");
        db.importDatabase(is);

        // the legacy db should have 8 tracks (6 active)
        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(2));
        assertThat(dataSource.getActiveTracks().size(), is(2));
        assertThat(dataSource.getTickCount(1), is(5));
        assertThat(dataSource.getTickCount(2), is(4));
        // make sure that no groups have been imported with a version 12 database
        // (groups did not exist back then)
        assertThat(dataSource.getGroups().size(), is(0));
        closeMethod.invoke(dataSource);
    }

    @Test
    public void legacyDatabaseVersion13ShouldBeImportable() throws Exception {
        InputStream is = tickmate.getAssets().open("test/tickmate-version13.db");
        File intDb = tickmate.getApplicationContext().getDatabasePath("tickmate.db");

        DatabaseOpenHelper db = DatabaseOpenHelper.getInstance(tickmate);
        db.importDatabase(is);

        // the legacy db should have 8 tracks (6 active)
        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(3));
        assertThat(dataSource.getActiveTracks().size(), is(3));
        assertThat(dataSource.getTickCount(1), is(6));
        assertThat(dataSource.getTickCount(2), is(4));
        assertThat(dataSource.getTickCount(3), is(5));
        // make sure that 3 groups have been imported
        assertThat(dataSource.getGroups().size(), is(3));
        assertThat(dataSource.getGroupsForTrack(1).size(), is(1));
        assertThat(dataSource.getGroupsForTrack(2).size(), is(3));
        assertThat(dataSource.getGroupsForTrack(3).size(), is(1));
        assertThat(dataSource.getGroups().get(1).getName(), is("Wochenende"));
        assertThat(dataSource.getTrack(3).multipleEntriesEnabled(), is(true));
        assertThat(dataSource.getTrack(2).multipleEntriesEnabled(), is(false));

        assertThat(dataSource.getTrack(1).getTickColor().getColorValue(), is(0x4ea6e0));

        closeMethod.invoke(dataSource);
    }

    @Test
    public void databaseGroupOrderTest() throws Exception {
        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(0));
        closeMethod.invoke(dataSource);

        Track t1 = new Track("Track 1", "Count to one");
        Track t2 = new Track("Track 2", "Count to two");
        Track t3 = new Track("Track 3", "Count to three");
        Track t4 = new Track("Track 4", "Count to four");
        Track t5 = new Track("Track 5", "Count to five");

        t1.setEnabled(true);
        t1.setOrder(1);
        t2.setEnabled(true);
        t2.setOrder(2);
        t3.setEnabled(true);
        t3.setOrder(3);
        t4.setEnabled(true);
        t4.setOrder(4);
        t5.setEnabled(true);
        t5.setOrder(5);

        openMethod.invoke(dataSource);
        /* store in random order */
        dataSource.storeTrack(t5);
        dataSource.storeTrack(t2);
        dataSource.storeTrack(t3);
        dataSource.storeTrack(t1);
        dataSource.storeTrack(t4);
        closeMethod.invoke(dataSource);

        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(5));

        assertThat(dataSource.getTracks().get(0).getName(), is("Track 1"));
        assertThat(dataSource.getTracks().get(1).getName(), is("Track 2"));
        assertThat(dataSource.getTracks().get(2).getName(), is("Track 3"));
        assertThat(dataSource.getTracks().get(3).getName(), is("Track 4"));
        assertThat(dataSource.getTracks().get(4).getName(), is("Track 5"));

        closeMethod.invoke(dataSource);

        Group g = new Group("My Group");
        g.setOrder(1);

        openMethod.invoke(dataSource);
        dataSource.storeGroup(g);
        dataSource.linkOneTrackOneGroup(t4.getId(), g.getId());
        dataSource.linkOneTrackOneGroup(t1.getId(), g.getId());
        dataSource.linkOneTrackOneGroup(t3.getId(), g.getId());
        dataSource.linkOneTrackOneGroup(t2.getId(), g.getId());
        closeMethod.invoke(dataSource);

        openMethod.invoke(dataSource);
        List<Track> ts = dataSource.getTracksForGroup(g.getId());
        assertThat(ts.get(0).getName(), is("Track 1"));
        assertThat(ts.get(1).getName(), is("Track 2"));
        assertThat(ts.get(2).getName(), is("Track 3"));
        assertThat(ts.get(3).getName(), is("Track 4"));

        closeMethod.invoke(dataSource);

    }


    @Test
    public void legacyDatabaseVersion14ShouldBeImportable() throws Exception {
        InputStream is = tickmate.getAssets().open("test/tickmate-version14.db");
        File intDb = tickmate.getApplicationContext().getDatabasePath("tickmate.db");

        DatabaseOpenHelper db = DatabaseOpenHelper.getInstance(tickmate);
        db.importDatabase(is);

        // the legacy db should have 3 tracks
        openMethod.invoke(dataSource);
        assertThat(dataSource.getTracks().size(), is(3));
        assertThat(dataSource.getActiveTracks().size(), is(3));
        assertThat(dataSource.getTickCount(1), is(23));

        assertThat(dataSource.getTrack(1).getTickColor().getColorValue(), is(0x4ea6e0));
        assertThat(dataSource.getTrack(2).getTickColor().getColorValue(), is(48340));

        closeMethod.invoke(dataSource);

    }
}
