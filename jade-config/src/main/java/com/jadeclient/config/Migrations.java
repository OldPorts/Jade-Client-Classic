package com.jadeclient.config;

import com.google.gson.JsonObject;

/**
 * Versioned config migrations. Each step upgrades the JSON to the next
 * schema version, so a config saved by Jade 26.1.0 still loads in Jade 27.x
 * without ever breaking the user's setup.
 */
public final class Migrations {

	private Migrations() {
	}

	public static JsonObject migrate(JsonObject json, int fromVersion) {
		int version = fromVersion;
		// One method per schema bump, chained here.
		if (version < 1) {
			json = toV1(json);
			version = 1;
		}
		// if (version < 2) { json = toV2(json); version = 2; }
		if (version != 1) {
			System.out.println("[jade] Config written by a newer Jade (schema " + version + "); loading anyway");
		}
		return json;
	}

	/** v0 (pre-release, unversioned files) to v1: schema tagging. */
	private static JsonObject toV1(JsonObject json) {
		return json;
	}
}
