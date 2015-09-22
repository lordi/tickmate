package de.smasi.tickmate.tests;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.AboutActivity;
import de.smasi.tickmate.views.ShowTrackActivity;

@Config(emulateSdk = 17) 
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
		TracksDataSource ds = TracksDataSource.getInstance();
		
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
	/*
	@Test
	public void editTrackActivityStoresChanges() throws Exception {
		Tickmate tm = new Tickmate();
		TracksDataSource ds = new TracksDataSource(tm);
		
		ds.open();
		Track t = new Track("Testing", "Run my tests");
		t.setEnabled(true);
		ds.storeTrack(t);
		ds.close();
		assertThat(t, equalTo(t));
		
		Intent i = new Intent(Robolectric.application.getApplicationContext(), tm.getClass());
		i.putExtra("track_id", t.getId());
		ActivityController<EditTrackActivity> r_eta = Robolectric.buildActivity(EditTrackActivity.class)
			  	.withIntent(i)
                .create(new Bundle())
                .start();
		
		EditTrackActivity eta = r_eta.get();
		EditText edit_description = (EditText) eta.findViewById(R.id.edit_description);
		assertThat(edit_description.getText().toString(), is("Run my tests"));
		edit_description.setText("Krishna Hare");
		r_eta.pause();
		r_eta.stop();
		
		Track t_also = ds.getTrack(t.getId());
		
		assertThat(t, equalTo(t_also));
		assertThat(t_also.getName(), is(t.getName()));
		assertThat(t_also.getIcon(), is(t.getIcon()));
		assertThat(t_also.getDescription(), is("Krishna Hare"));
		assertThat(t_also.isEnabled(), is(t.isEnabled()));
	}	
*/
	@Test
	public void tickmateAboutActivityShouldRunFine() throws Exception {

		// make sure that about activity does not throw error
		Robolectric.buildActivity(AboutActivity.class).
			create(new Bundle());
		
		// TODO make sure that version appear in AboutActivity
	}

}
