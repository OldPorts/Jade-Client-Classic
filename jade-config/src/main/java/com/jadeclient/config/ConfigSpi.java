package com.jadeclient.config;

import com.google.gson.JsonObject;

/**
 * SPI implemented by jade-core and installed at boot. Lets jade-config
 * persist module state without depending on jade-core types, avoiding a
 * circular dependency.
 */
public interface ConfigSpi {

	/** Reads module state from JSON. */
	void applyModuleState(JsonObject modules);

	/** Serializes current module state to JSON. */
	JsonObject moduleState();

	/** Logging hook so jade-config never needs the root logger. */
	void logError(String message, Throwable error);

	void logWarn(String message);
}
