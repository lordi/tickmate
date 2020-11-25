package de.smasi.tickmate;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.AboutActivity;
import de.smasi.tickmate.views.ShowTrackActivity;
import de.smasi.tickmate.views.TrackPreferenceActivity;
import de.smasi.tickmate.prefs.TrackPreferenceFragment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Config(sdk = Build.VERSION_CODES.O)
@RunWith(RobolectricTestRunner.class)
public class ActivitiesTest {
    Tickmate tickmate;
    DataSource dataSource;

    Method openMethod, closeMethod;

    @Before
    public void setUp() throws NoSuchMethodException {
        tickmate = new Tickmate();
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
    public void showTrackActivityShouldNotCrashWhenNoIntent() throws Exception {
        Robolectric.buildActivity(ShowTrackActivity.class)
                .create(new Bundle())
                .start();
    }

    @Test
    public void showTrackActivityShouldNotCrashWhenNoTicks() throws Exception {
        openMethod.invoke(dataSource);
        Track t = new Track("Testing", "Run my tests");
        t.setEnabled(true);
        dataSource.storeTrack(t);
        closeMethod.invoke(dataSource);

        Intent i = new Intent(RuntimeEnvironment.application.getApplicationContext(), tickmate.getClass());
        i.putExtra("track_id", t.getId());
        Robolectric.buildActivity(ShowTrackActivity.class, i)
                .create(new Bundle())
                .start();
    }

    @Test
    public void editTrackActivityStoresChanges() throws Exception {
        openMethod.invoke(dataSource);
        Track t = new Track("Testing", "Run my tests");
        t.setEnabled(true);
        dataSource.storeTrack(t);
        closeMethod.invoke(dataSource);
        assertThat(t, equalTo(t));

        Intent i = new Intent(RuntimeEnvironment.application.getApplicationContext(), tickmate.getClass());
        i.putExtra("track_id", t.getId());
        ActivityController<TrackPreferenceActivity> r_eta = Robolectric.buildActivity(TrackPreferenceActivity.class, i)
                .create(new Bundle())
                .start()
                .resume();

        TrackPreferenceActivity eta = r_eta.get();
        TrackPreferenceFragment tpf = (TrackPreferenceFragment) eta.getFragmentManager().findFragmentById(android.R.id.content);
        EditTextPreference description = (EditTextPreference) tpf.findPreference("description");
        assertThat(description.getText().toString(), is("Run my tests"));
        description.setText("Krishna Hare");

        r_eta.pause();
        r_eta.stop();

        Track t_also = dataSource.getTrack(t.getId());

        assertThat(t, equalTo(t_also));
        assertThat(t_also.getName(), is(t.getName()));
        assertThat(t_also.getIcon(), is(t.getIcon()));
        assertThat(t_also.getDescription(), is("Krishna Hare"));
        assertThat(t_also.isEnabled(), is(t.isEnabled()));
    }

    @Test
    public void tickmateAboutActivityShouldRunFine() throws Exception {

        // make sure that about activity does not throw error
        Robolectric.buildActivity(AboutActivity.class).
                create(new Bundle());

        // TODO make sure that version appear in AboutActivity
    }

}
