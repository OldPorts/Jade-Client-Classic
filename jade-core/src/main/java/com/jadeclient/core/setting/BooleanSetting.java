package com.jadeclient.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A yes/no toggle. */
public class BooleanSetting extends Setting<Boolean> {

	public BooleanSetting(String id, boolean defaultValue) {
		super(id, defaultValue);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(get());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			set(element.getAsBoolean());
		}
	}
}
