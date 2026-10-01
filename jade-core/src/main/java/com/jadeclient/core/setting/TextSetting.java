package com.jadeclient.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A free-form single line of text (custom text HUD module, macros). */
public class TextSetting extends Setting<String> {

	public TextSetting(String id, String defaultValue) {
		super(id, defaultValue);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(get());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			set(element.getAsString());
		}
	}
}
