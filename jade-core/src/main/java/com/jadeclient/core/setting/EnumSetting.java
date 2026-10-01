package com.jadeclient.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

/** A single choice out of a fixed set of named options. */
public class EnumSetting extends Setting<String> {

	private final List<String> options;

	public EnumSetting(String id, String defaultValue, List<String> options) {
		super(id, defaultValue);
		this.options = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(options));
	}

	public List<String> options() {
		return options;
	}

	@Override
	protected String clamp(String value) {
		return options.contains(value) ? value : get();
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
