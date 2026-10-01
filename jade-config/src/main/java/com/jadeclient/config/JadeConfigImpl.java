package com.jadeclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * JSON config store rooted at .minecraft/jade/.
 *
 * Layout:
 *   jade/config.json            global + module state for the active profile
 *   jade/profiles/<name>.json   named profiles
 *
 * Every save bumps a schema version; load() runs migrations so old configs
 * never break on upgrade.
 */
public final class JadeConfigImpl extends JadeConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final int SCHEMA_VERSION = 1;

	private final Path root;
	private final ConfigSpi spi;
	private String activeProfile = "default";

	public JadeConfigImpl(Path root, ConfigSpi spi) {
		this.root = root;
		this.spi = spi;
		JadeConfig.setInstance(this);
	}

	@Override
	public Path root() {
		return root;
	}

	@Override
	public void load() {
		try {
			Files.createDirectories(root);
			Path file = root.resolve("config.json");
			if (!Files.exists(file)) {
				save();
				return;
			}
			JsonObject json = GSON.fromJson(readAll(file), JsonObject.class);
			int version = json.has("schema") ? json.get("schema").getAsInt() : 0;
			json = Migrations.migrate(json, version);

			activeProfile = json.has("activeProfile") ? json.get("activeProfile").getAsString() : "default";
			JsonObject modules = json.getAsJsonObject("modules");
			if (modules != null) {
				spi.applyModuleState(modules);
			}
		} catch (Exception e) {
			spi.logError("Failed to load Jade config; keeping defaults", e);
		}
	}

	@Override
	public void save() {
		try {
			Files.createDirectories(root);
			Path file = root.resolve("config.json");
			Path tmp = root.resolve("config.json.tmp");

			JsonObject json = new JsonObject();
			json.addProperty("schema", SCHEMA_VERSION);
			json.addProperty("activeProfile", activeProfile);
			json.add("modules", spi.moduleState());

			writeAll(tmp, GSON.toJson(json));
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (Exception e) {
			spi.logError("Failed to save Jade config", e);
		}
	}

	@Override
	public void export(Path target) {
		try {
			if (target.getParent() != null) {
				Files.createDirectories(target.getParent());
			}
			JsonObject json = new JsonObject();
			json.addProperty("schema", SCHEMA_VERSION);
			json.addProperty("exportedFrom", "jade-client");
			json.add("modules", spi.moduleState());
			writeAll(target, GSON.toJson(json));
		} catch (IOException e) {
			spi.logError("Failed to export config to " + target, e);
		}
	}

	@Override
	public void import_(Path source) {
		try {
			JsonObject json = GSON.fromJson(readAll(source), JsonObject.class);
			int version = json.has("schema") ? json.get("schema").getAsInt() : 0;
			json = Migrations.migrate(json, version);
			if (json.has("modules")) {
				spi.applyModuleState(json.getAsJsonObject("modules"));
				save();
			}
		} catch (Exception e) {
			spi.logError("Failed to import config from " + source, e);
		}
	}

	@Override
	public String activeProfile() {
		return activeProfile;
	}

	@Override
	public void setActiveProfile(String name) {
		// Save current state into the old profile before switching.
		saveProfile(activeProfile);
		this.activeProfile = name;
		Path profileFile = root.resolve("profiles").resolve(name + ".json");
		if (Files.exists(profileFile)) {
			try {
				JsonObject json = GSON.fromJson(readAll(profileFile), JsonObject.class);
				if (json.has("modules")) {
					spi.applyModuleState(json.getAsJsonObject("modules"));
				}
			} catch (Exception e) {
				spi.logError("Failed to load profile " + name, e);
			}
		}
		save();
	}

	private void saveProfile(String name) {
		try {
			Path dir = root.resolve("profiles");
			Files.createDirectories(dir);
			JsonObject json = new JsonObject();
			json.addProperty("schema", SCHEMA_VERSION);
			json.add("modules", spi.moduleState());
			writeAll(dir.resolve(name + ".json"), GSON.toJson(json));
		} catch (IOException e) {
			spi.logError("Failed to save profile " + name, e);
		}
	}

	// -- Java 8 file helpers (Files.readString/writeString are Java 11+) ----

	private static String readAll(Path file) throws IOException {
		return new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8);
	}

	private static void writeAll(Path file, String text) throws IOException {
		Files.write(file, text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
}
