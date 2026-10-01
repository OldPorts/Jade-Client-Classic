package com.jadeclient.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A floating-point setting rendered as a slider in the UI. */
public class FloatSetting extends Setting<Float> {

	private final float min;
	private final float max;

	public FloatSetting(String id, float defaultValue, float min, float max) {
		super(id, defaultValue);
		this.min = min;
		this.max = max;
	}

	public float min() {
		return min;
	}

	public float max() {
		return max;
	}

	@Override
	protected Float clamp(Float value) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(get());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			set(element.getAsFloat());
		}
	}
}
