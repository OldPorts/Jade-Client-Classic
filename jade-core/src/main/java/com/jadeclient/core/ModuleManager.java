package com.jadeclient.core;

import com.jadeclient.core.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owns the full module list. Registration order within a category is the
 * order modules appear in the ClickGUI.
 */
public final class ModuleManager {

	public ModuleManager() {
	}

	private final List<Module> modules = new ArrayList<>();

	public void register(Module module) {
		modules.add(module);
	}

	public List<Module> modules() {
		return Collections.unmodifiableList(modules);
	}

	public List<Module> byCategory(Category category) {
		List<Module> out = new ArrayList<>();
		for (Module module : modules) {
			if (module.category() == category) {
				out.add(module);
			}
		}
		return out;
	}

	public Module byId(String id) {
		for (Module module : modules) {
			if (module.id().equals(id)) {
				return module;
			}
		}
		return null;
	}

	/** Handles a raw LWJGL key press; returns true if a module consumed it. */
	public boolean handleKeybind(int key) {
		for (Module module : modules) {
			if (module.keybind() == key) {
				module.toggle();
				return true;
			}
		}
		return false;
	}

	public void onWorldUnload() {
		for (Module module : modules) {
			try {
				module.onWorldUnload();
			} catch (Exception e) {
				// Graceful degradation: one broken module never breaks the rest.
				com.jadeclient.JadeClient.LOGGER.error("Module {} failed world unload", module.id(), e);
			}
		}
	}

	public void init() {
		EventBus.get().subscribe(EventBus.ClientTick.class, e -> tick());
	}

	private void tick() {
		for (Module module : modules) {
			if (!module.isEnabled()) {
				continue;
			}
			if (!(module instanceof Ticked)) {
				continue;
			}
			Ticked ticked = (Ticked) module;
			try {
				ticked.tick();
			} catch (Exception e) {
				com.jadeclient.JadeClient.LOGGER.error("Module {} tick failed, disabling", module.id(), e);
				module.setEnabled(false);
			}
		}
	}

	/** Modules that want per-tick updates implement this. */
	public interface Ticked {
		void tick();
	}
}
