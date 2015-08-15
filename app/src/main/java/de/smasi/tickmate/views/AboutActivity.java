package de.smasi.tickmate.views;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.DatabaseOpenHelper;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView title = (TextView) findViewById(R.id.about_title);
		TextView desc = (TextView) findViewById(R.id.about_description);
		PackageInfo pInfo;
		String version;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			version = "";
		}
		title.setText(getString(R.string.app_name) + " " + version);
		
		try {
			DatabaseOpenHelper db = new DatabaseOpenHelper(this);
			desc.setText(getString(R.string.about_description) + "\n\nBackup folder: " + db.getExternalDatabaseFolder().getAbsoluteFile());
		} catch (IOException e) {
			// TODO: handle exception
		}
		
	}

}
