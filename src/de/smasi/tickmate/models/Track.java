package de.smasi.tickmate.models;

import android.content.Context;
import android.content.res.Resources;

public class Track {
	String name;
	String description;
	String icon;
	int id;
	boolean enabled;
	int iconId;
	
	public Track(String name) {
		super();
		this.name = name;
		this.id = 0;
		this.enabled = true;
		this.description = "";
		this.iconId = -1;
		this.icon = "glyphicons_001_leaf_white";
	}

	public Track(String name, String description) {
		super();
		this.name = name;
		this.id = 0;
		this.enabled = true;
		this.description = description;
		this.iconId = -1;
		this.icon = "glyphicons_001_leaf_white";
	}

	public int getIconId(Context ctx) {
		if (this.iconId == -1) {
			Resources r = ctx.getResources();
			this.iconId = r.getIdentifier(this.icon, "drawable", ctx.getPackageName());
		}
		return this.iconId;
	}
	
	public boolean isGroupHeader() {
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
