package com.jadeclient.config;

import java.io.File;
import java.nio.file.Path;

/**
 * jade-config early bootstrap: instantiates the config store (with a no-op
 * SPI) before the module registry boots. {@code JadeClient.boot} then
 * re-installs the real store with its SPI implementation.
 */
public final class ConfigInit {

	private ConfigInit() {
	}

	public static void initEarly(File gameDir) {
		Path root = gameDir.toPath().resolve("jade");
		new JadeConfigImpl(root, new ConfigSpi() {
			@Override
			public void applyModuleState(com.google.gson.JsonObject modules) {
			}

			@Override
			public com.google.gson.JsonObject moduleState() {
				return new com.google.gson.JsonObject();
			}

			@Override
			public void logError(String message, Throwable error) {
				System.err.println("[jade-config] " + message);
				if (error != null) {
					error.printStackTrace();
				}
			}

			@Override
			public void logWarn(String message) {
				System.out.println("[jade-config] " + message);
			}
		});
	}
}
