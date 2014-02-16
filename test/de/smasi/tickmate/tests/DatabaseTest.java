package de.smasi.tickmate.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;
import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;

@RunWith(RobolectricTestRunner.class)
public class DatabaseTest {

	@Test
	public void databaseCreateExportImport() throws Exception {
		Tickmate tm = new Tickmate();
		TracksDataSource ds = new TracksDataSource(tm);
		ds.open();
		assertThat(ds.getMyTracks().size(), is(0));
		ds.close();
		Track t = new Track("Testing", "Run my tests");
		t.setEnabled(true);
		assertThat(t.isEnabled(), is(true));
		assertThat(t.isGroupHeader(), is(false));
		ds.open();
		ds.storeTrack(t);
		ds.close();
		ds.open();
		assertThat(ds.getMyTracks().size(), is(1));
		ds.close();

		DatabaseOpenHelper db = DatabaseOpenHelper.getInstance(tm);
		db.exportDatabase("test.db");
		db.exportDatabase("test2.db");
		assertThat(Arrays.asList(db.getExternalDatabaseNames()),
				hasItem("test.db"));
		assertThat(Arrays.asList(db.getExternalDatabaseNames()),
				hasItem("test2.db"));

		Track t2 = new Track("Testing 2", "Run my tests again");
		ds.open();
		ds.storeTrack(t2);
		ds.close();
		ds.open();
		assertThat(ds.getMyTracks().size(), is(2));
		ds.close();

		db.importDatabase("test.db");

		// reimported previous database, so track count should be one again:
		ds.open();
		assertThat(ds.getMyTracks().size(), is(1));
		ds.close();
	}

	@Test
	public void tickmateShouldAppearInDescription() throws Exception {
		String hello = new Tickmate().getResources().getString(
				R.string.about_description);
		assertThat(hello, containsString("Tickmate"));

	}

}
