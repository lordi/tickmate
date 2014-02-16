package de.smasi.tickmate.tests;

import java.util.Arrays;
import java.util.LinkedList;

import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;
import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.AboutActivity;
import de.smasi.tickmate.views.ShowTrackActivity;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.os.Bundle;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ActivitiesTest {
	@Test
	public void showTrackActivityShouldNotCrashWhenNoIntent() throws Exception {
		Tickmate tm = new Tickmate();
		Robolectric.buildActivity(ShowTrackActivity.class)
		        .create(new Bundle())
                .start();
	}
	
	@Test
	public void showTrackActivityShouldNotCrashWhenNoTicks() throws Exception {
		Tickmate tm = new Tickmate();
		TracksDataSource ds = new TracksDataSource(tm);
		
		ds.open();
		Track t = new Track("Testing", "Run my tests");
		t.setEnabled(true);
		ds.storeTrack(t);
		ds.close();
		
		Intent i = new Intent(Robolectric.application.getApplicationContext(), tm.getClass());
		i.putExtra("track_id", t.getId());
		Robolectric.buildActivity(ShowTrackActivity.class)
			  	.withIntent(i)
                .create(new Bundle())
                .start();
	}

	@Test
	public void tickmateAboutActivityShouldRunFine() throws Exception {

		// make sure that about activity does not throw error
		Robolectric.buildActivity(AboutActivity.class).
			create(new Bundle());
		
		// TODO make sure that version appear in AboutActivity
	}

}
