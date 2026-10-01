package com.jadeclient.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A whole-number setting rendered as a slider in the UI. */
public class IntSetting extends Setting<Integer> {

	private final int min;
	private final int max;

	public IntSetting(String id, int defaultValue, int min, int max) {
		super(id, defaultValue);
		this.min = min;
		this.max = max;
	}

	public int min() {
		return min;
	}

	public int max() {
		return max;
	}

	@Override
	protected Integer clamp(Integer value) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(get());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			set(element.getAsInt());
		}
	}
}
