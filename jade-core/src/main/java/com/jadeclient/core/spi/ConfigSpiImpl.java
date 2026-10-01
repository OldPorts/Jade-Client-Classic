package com.jadeclient.core.spi;

import com.google.gson.JsonObject;
import com.jadeclient.JadeClient;
import com.jadeclient.config.ConfigSpi;
import com.jadeclient.core.Module;
import com.jadeclient.core.ModuleManager;
import com.jadeclient.core.setting.Setting;

/**
 * jade-core's implementation of the config SPI: translates module state to
 * and from JSON. Lives here (not in jade-config) so the dependency arrow
 * points one way: core -> config.
 */
public final class ConfigSpiImpl implements ConfigSpi {

	private final ModuleManager manager;

	public ConfigSpiImpl(ModuleManager manager) {
		this.manager = manager;
	}

	@Override
	public void applyModuleState(JsonObject modules) {
		for (Module module : manager.modules()) {
			if (!modules.has(module.id())) {
				continue;
			}
			JsonObject m = modules.getAsJsonObject(module.id());
			if (m.has("enabled")) {
				try {
					module.setEnabled(m.get("enabled").getAsBoolean());
				} catch (Exception e) {
					JadeClient.LOGGER.warn("Could not restore enabled state for {}", module.id());
				}
			}
			if (m.has("keybind")) {
				module.setKeybind(m.get("keybind").getAsInt());
			}
			if (m.has("settings")) {
				JsonObject settings = m.getAsJsonObject("settings");
				for (Setting<?> setting : module.settings()) {
					if (settings.has(setting.id())) {
						try {
							setting.fromJson(settings.get(setting.id()));
						} catch (Exception e) {
							JadeClient.LOGGER.warn("Bad value for {}.{}", module.id(), setting.id());
						}
					}
				}
			}
		}
	}

	@Override
	public JsonObject moduleState() {
		JsonObject modules = new JsonObject();
		for (Module module : manager.modules()) {
			JsonObject m = new JsonObject();
			m.addProperty("enabled", module.isEnabled());
			m.addProperty("keybind", module.keybind());
			JsonObject settings = new JsonObject();
			for (Setting<?> setting : module.settings()) {
				settings.add(setting.id(), setting.toJson());
			}
			m.add("settings", settings);
			modules.add(module.id(), m);
		}
		return modules;
	}

	@Override
	public void logError(String message, Throwable error) {
		JadeClient.LOGGER.error(message, error);
	}

	@Override
	public void logWarn(String message) {
		JadeClient.LOGGER.warn(message);
	}
}
