package de.smasi.tickmate.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;
import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.database.FileUtils;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;

@Config(emulateSdk = 17) 
@RunWith(RobolectricTestRunner.class)
public class DatabaseTest {

	@Test
	public void databaseCreateExportImport() throws Exception {
		Tickmate tm = new Tickmate();
		TracksDataSource ds = new TracksDataSource(tm);
		ds.open();
		assertThat(ds.getTracks().size(), is(0));
		ds.close();
		Track t = new Track("Testing", "Run my tests");
		t.setEnabled(true);
		assertThat(t.isEnabled(), is(true));
		assertThat(t.isGroupHeader(), is(false));
		ds.open();
		ds.storeTrack(t);
		ds.close();
		ds.open();
		assertThat(ds.getTracks().size(), is(1));
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
		assertThat(ds.getTracks().size(), is(2));
		ds.close();

		db.importDatabase("test.db");

		// reimported previous database, so track count should be one again:
		ds.open();
		assertThat(ds.getTracks().size(), is(1));
		ds.close();
	}

	@Test
	public void tickmateShouldAppearInDescription() throws Exception {
		String hello = new Tickmate().getResources().getString(
				R.string.about_description);
		assertThat(hello, containsString("Tickmate"));

	}

	@Test
	public void trackDefaultIconResolvesToResID() throws Exception {
		Track t = new Track("Test track", "Cats and dogs");
		assertThat(t.getDescription(), is("Cats and dogs"));
		assertThat(t.getIcon(), not(is("")));
		assertThat(t.getIcon(), not(nullValue()));
		assert (t.getIconId(Robolectric.application.getApplicationContext()) > 0);
	}

	@Test
	public void legacyDatabaseVersion10ShouldBeImportable() throws Exception {
		// File testDb = new File(getClass().getResource("test.sql").getFile());
		Tickmate tm = new Tickmate();
		InputStream is = tm.getAssets().open("test/smiley-version10.db");
		DatabaseOpenHelper db = DatabaseOpenHelper.getInstance(tm);
		File extDb = new File(db.getExternalDatabasePath("smiley.db"));

		FileUtils.saveStreamToFile(is, new FileOutputStream(extDb));
		db.importDatabase("smiley.db");

		// the legacy db should have 8 tracks (6 active)
		TracksDataSource ds = new TracksDataSource(tm);
		ds.open();
		assertThat(ds.getTracks().size(), is(8));
		assertThat(ds.getActiveTracks().size(), is(6));
		assertThat(ds.getTickCount(1), is(28));
		assertThat(ds.getTickCount(2), is(2));
		assertThat(ds.getTickCount(3), is(13));
		ds.close();
	}
}
