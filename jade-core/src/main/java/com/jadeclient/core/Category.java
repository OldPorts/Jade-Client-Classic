package com.jadeclient.core;

/**
 * Top-level categories shown in the ClickGUI sidebar. Kept deliberately
 * small so the sidebar stays scannable.
 */
public enum Category {
	PERFORMANCE("Performance"),
	HUD("HUD"),
	VISUALS("Visuals"),
	COSMETICS("Cosmetics"),
	QOL("QoL"),
	SETTINGS("Settings");

	private final String displayName;

	Category(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
