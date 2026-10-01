package com.jadeclient.config;

import java.nio.file.Path;

/**
 * Central config access singleton. Lives in jade-config so both core and UI
 * modules can reach it; the implementation is {@link JadeConfigImpl}.
 */
public abstract class JadeConfig {

	private static JadeConfig instance;

	public static JadeConfig get() {
		return instance;
	}

	public static void setInstance(JadeConfig impl) {
		instance = impl;
	}

	/** Config root: .minecraft/jade/ */
	public abstract Path root();

	/** Reads the config from disk, applying versioned migrations on the way. */
	public abstract void load();

	/** Persists the current state (all modules + global settings). */
	public abstract void save();

	/** Exports everything into one portable JSON file. */
	public abstract void export(Path target);

	/** Imports a previously exported file, replacing current state. */
	public abstract void import_(Path source);

	/** Active profile name (per-server profiles switch this at runtime). */
	public abstract String activeProfile();

	public abstract void setActiveProfile(String name);
}
