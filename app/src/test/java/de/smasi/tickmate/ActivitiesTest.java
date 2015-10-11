package de.smasi.tickmate;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.AboutActivity;
import de.smasi.tickmate.views.ShowTrackActivity;
import de.smasi.tickmate.views.TrackPreferenceActivity;
import de.smasi.tickmate.views.TrackPreferenceFragment;

@Config(sdk = 17, constants = BuildConfig.class)
@RunWith(TickmateTestRunner.class)
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
        Robolectric.buildActivity(ShowTrackActivity.class)
                .withIntent(i)
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

//        EditTrackActivity doesn't exist, and TrackPreferenceActivity seems the closest analog:

        Intent i = new Intent(RuntimeEnvironment.application.getApplicationContext(), tickmate.getClass());
        i.putExtra("track_id", t.getId());
        ActivityController<TrackPreferenceActivity> r_eta = Robolectric.buildActivity(TrackPreferenceActivity.class)
                .withIntent(i)
                .create(new Bundle())
                .start()
                .resume();

        TrackPreferenceActivity eta = r_eta.get();
        // AVP and/or HG, check this?
        // I've changed this code...
//        EditText edit_description = (EditText) eta.findViewById(R.id.edit_description);
//        assertThat(edit_description.getText().toString(), is("Run my tests"));
//        edit_description.setText("Krishna Hare");
        //  ... to this...
        TrackPreferenceFragment tpf = (TrackPreferenceFragment) eta.getFragmentManager().findFragmentById(android.R.id.content);
        EditTextPreference description = (EditTextPreference) tpf.findPreference("description");
        assertThat(description.getText().toString(), is("Run my tests"));
        description.setText("Krishna Hare");

        // ...because R.id.edit_description no longer exists, and EditTrackActivity contains
        // a TrackPreferenceFragment.

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
