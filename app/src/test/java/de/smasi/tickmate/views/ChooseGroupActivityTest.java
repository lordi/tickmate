package de.smasi.tickmate.views;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import de.smasi.tickmate.models.Group;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@Config(sdk = Build.VERSION_CODES.O)
@RunWith(RobolectricTestRunner.class)
public class ChooseGroupActivityTest {

    private ActivityController<ChooseGroupActivity> mActivityController;
    private ChooseGroupActivity mActivity;

    @Before
    public void setUp() {
        mActivityController = Robolectric.buildActivity(ChooseGroupActivity.class);
        mActivity = mActivityController.create().start().get();
    }

    @After
    public void tearDown() {
        mActivityController = mActivityController.pause().stop().destroy();
        mActivity = null;
    }

    @Test
    public void test_onListItemClick() {
        // create a Group that is not a section header
        Group g = new Group("groupName");
        g.setSectionHeader(false);
        g.setId(0);

        // set adapter
        GroupListAdapter groupListAdapter = new GroupListAdapter(mActivity, new Group[] { g });
        mActivity.getListView().setAdapter(groupListAdapter);

        // click on Group
        mActivity.onListItemClick(mActivity.getListView(), null, 0, 0l);

        // verify that activity is finishing
        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void test_onListItemClick_header() {
        // create a Group that is a section header
        Group g = new Group("groupName");
        g.setSectionHeader(true);
        g.setId(0);

        // set adapter
        GroupListAdapter groupListAdapter = new GroupListAdapter(mActivity, new Group[] { g });
        mActivity.getListView().setAdapter(groupListAdapter);

        // click on Group
        mActivity.onListItemClick(mActivity.getListView(), null, 0, 0l);

        // verify that activity is not finishing
        assertFalse(mActivity.isFinishing());
    }
}
