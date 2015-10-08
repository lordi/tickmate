package de.smasi.tickmate.views;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Group;
//import de.smasi.tickmate.models.Track;

public class ChooseGroupActivity extends ListActivity {

    ArrayAdapter<Group> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_edit_tracks);
        // Show the Up button in the action bar.
        setupActionBar();

        XmlResourceParser xrp = getResources().getXml(getResources().getIdentifier("groups", "xml", getPackageName()));

        int eventType;
        List<Group> listOfGroups = new LinkedList<>();

        try {
            eventType = xrp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.v("XML", "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    Log.v("XML", "Start tag " + xrp.getName());
                    if (xrp.getName().equals("group")) {
                        String name = xrp.getAttributeValue(null, "name");
                        String description = xrp.getAttributeValue(null, "description");
                        if (name == null || description == null) {
                            Log.w("Tickmate", "groups.xml. Ignoring entry.");
                        }
                        Group g = new Group(name, description);
                        // Ignore the icon returned, for now
//	        		 g.setIcon(getResources().getResourceEntryName(xrp.getAttributeResourceValue(null, "icon", R.drawable.glyphicons_000_glass_white)));
                        listOfGroups.add(g);
                    } else if (xrp.getName().equals("section")) {
                        Group g = new Group(xrp.getAttributeValue(null, "name"));
                        g.setSectionHeader(true);
                        listOfGroups.add(g);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    Log.v("XML", "End tag " + xrp.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    Log.v("XML", "Text " + xrp.getText());
                }
                eventType = xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e("XML", "XmlPullParserException: " + e.getMessage());
        } catch (IOException e) {
            Log.e("XML", "IOException: " + e.getMessage());
        }

        Log.v("XML", listOfGroups.size() + " groups loaded");

        Group[] groupList = new Group[listOfGroups.size()];
        listOfGroups.toArray(groupList);
        groups = new GroupListAdapter(this, groupList);
        this.getListView().setAdapter(groups);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Group g = (Group) getListView().getAdapter().getItem(position);

        if (!g.isSectionHeader()) {
            DataSource.getInstance().storeGroup(g);
            Intent data = new Intent();
            data.putExtra("insert_id", g.getId());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

    }


}
