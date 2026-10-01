package com.jadeclient.core;

import com.jadeclient.core.event.EventBus;
import com.jadeclient.core.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for every Jade feature. A module is a toggleable unit with
 * optional settings and an optional HUD anchor (see HudModule in jade-ui for
 * the rendering half).
 */
public abstract class Module {

	private final String id;
	private final String name;
	private final Category category;
	private final String descriptionKey;

	private boolean enabled;
	private int keybind = -1; // -1 = unbound

	private final List<Setting<?>> settings = new ArrayList<>();

	protected Module(String id, String name, Category category, String descriptionKey) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.descriptionKey = descriptionKey;
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public Category category() {
		return category;
	}

	/** Translation key for the module description, e.g. jade.module.fps.desc */
	public String descriptionKey() {
		return descriptionKey;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
		EventBus.get().post(new EventBus.ModuleToggled(this, enabled));
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	/** Called when the module is turned on. */
	protected void onEnable() {
	}

	/** Called when the module is turned off. */
	protected void onDisable() {
	}

	public int keybind() {
		return keybind;
	}

	public void setKeybind(int keybind) {
		this.keybind = keybind;
	}

	public List<Setting<?>> settings() {
		return Collections.unmodifiableList(settings);
	}

	protected void register(Setting<?>... newSettings) {
		Collections.addAll(settings, newSettings);
	}

	/**
	 * Modules that hold client state should reset it here so a crash mid-game
	 * never leaves stale state behind.
	 */
	public void onWorldUnload() {
	}
}
