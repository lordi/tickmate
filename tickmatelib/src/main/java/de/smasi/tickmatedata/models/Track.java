package de.smasi.tickmatedata.models;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;


public class Track implements Serializable {
    private static final String TAG = "Track";
	String name;
	String description;
	String icon;
	int mId;
	boolean enabled;
	boolean multiple_entries_enabled;
	int iconId;

	int order;

    public Track(String name) {
		super();
		this.name = name;
		this.mId = 0;
		this.enabled = true;
		this.description = "";
		this.iconId = -1;
		this.icon = "glyphicons_001_leaf_white";
		this.order = 0;
	}

	public Track(String name, String description) {
		super();
		this.name = name;
		this.mId = 0;
		this.enabled = true;
		this.description = description;
		this.iconId = -1;
		this.icon = "glyphicons_001_leaf_white";
		this.order = 0;
	}
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

    @Override
	public boolean equals(Object o) {
		Track other = (Track)o;
		return other.mId == mId;
	}	

	public int getIconId(Context ctx) {
		return getIconId(ctx, false);
	}

	public int getIconId(Context ctx, boolean dark) {
		if (this.iconId == -1) {
			Resources r = ctx.getResources();
			if (dark) {
				String iconName = this.icon.replace("_white", "");
				this.iconId = r.getIdentifier(iconName, "drawable", ctx.getPackageName());
			} else {
				this.iconId = r.getIdentifier(this.icon, "drawable", ctx.getPackageName());
			}
		}
		return this.iconId;
	}

    public boolean isSectionHeader() {

        return getName().startsWith("--- ");
    }

	public void setIcon(String resName) {
		this.iconId = -1;
		this.icon = resName;
	}

	public String getIcon() {
		return this.icon;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean multipleEntriesEnabled() {
		return multiple_entries_enabled;
	}

	public void setMultipleEntriesEnabled(boolean enabled) {
		this.multiple_entries_enabled = enabled;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCustomTrack() {
		return icon.contains("_star_");
	}

    // Used primarily for debugging.
    public String toString() {
        return "Group:  id(" + mId + ") name(" + name + ") description(" + description + ")";
    }

}
