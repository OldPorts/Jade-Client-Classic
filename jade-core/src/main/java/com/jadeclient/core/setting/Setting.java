package com.jadeclient.core.setting;

import com.google.gson.JsonElement;

/**
 * Base class for every module setting. Settings know how to serialize
 * themselves, which keeps module code free of persistence logic.
 */
public abstract class Setting<T> {

	private final String id;
	private final T defaultValue;
	private T value;

	protected Setting(String id, T defaultValue) {
		this.id = id;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public String id() {
		return id;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = clamp(value);
	}

	public void reset() {
		this.value = defaultValue;
	}

	/** Overridable range/clamp hook; identity by default. */
	protected T clamp(T value) {
		return value;
	}

	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement element);
}
