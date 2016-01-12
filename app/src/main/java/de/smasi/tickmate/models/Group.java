package de.smasi.tickmate.models;

/**
 * Groups are a way to organize and categorize Tracks. Groups can be predefined or user-defined.
 */
public class Group {

    // Used to indicate that 'all groups' have been selected for display
    public static final Group ALL_GROUP = new Group("__all__");

    private static final String TAG = "Group";

    private int mId = -1;         // Unique identifier will be assigned by database
    private String mName = "";
    private String mDescription = "";
    private boolean mIsSectionHeader = false; // For entries derived from groups.xml to be section headers
    private int mOrder = 0;

    /**
     * @param name group name
     */
    public Group(String name) {
        mName = name;
    }

    /**
     * @param name group name
     * @param description group description
     */
    public Group(String name, String description) {
        mName = name;
        mDescription = description;
    }

    /**
     * @return group id
     */
    public int getId() {
        return mId;
    }

    /**
     * @param id group id
     */
    public void setId(int id) {
        mId = id;
    }

    /**
     * @return group description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * @param description group description
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * @return group name
     */
    public String getName() {
        return mName;
    }

    /**
     * @param name group name
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * @return group name
     */
    public boolean isSectionHeader() {
        return mIsSectionHeader;
    }

    /**
     * Prints contents on Group object. Used primarily for debug purposes.
     */
    public String toString() {
//        return "Group:  id(" + mId + ") name(" + mName + ") description(" + mDescription + ")  trackIds(" + TextUtils.join(",", mTrackIds) + ")";
        return "Group:  id(" + mId + ") name(" + mName + ") description(" + mDescription + ")  trackIds(not stored here at the moment)";
    }

    /**
     * Only used when parsing groups.xml.
     *
     * @param sectionHeader group name
     */
    public void setSectionHeader(boolean sectionHeader) {
        mIsSectionHeader = sectionHeader;
    }

    public void setOrder(int groupOrder) {
//        Log.d(TAG, "Group.setOrder(" + groupOrder + ")");
        mOrder = groupOrder;
    }

    public int getOrder() {
        return mOrder;
    }
}

