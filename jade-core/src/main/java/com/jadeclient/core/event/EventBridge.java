package com.jadeclient.core.event;

import net.minecraft.client.Minecraft;

/**
 * Bridges Forge lifecycle events onto the Jade event bus so modules only
 * ever need to talk to {@link EventBus}. The Forge handler
 * {@code com.jadeclient.forge.JadeForgeEvents} drives this class.
 */
public final class EventBridge {

	private EventBridge() {
	}

	/** No-op on Forge: the event handler registers itself on the Forge bus. */
	public static void register() {
	}

	/** Fires the per-tick event. Called from the Forge client-tick handler. */
	public static void postTick() {
		EventBus.get().post(new EventBus.ClientTick());
	}

	/**
	 * Posts a frame event with the current FPS and frametime. Called from the
	 * FPS tracker each frame.
	 */
	public static void postFrame(float fps, float frameTimeMs) {
		EventBus.get().post(new EventBus.Frame(fps, frameTimeMs));
	}

	/** Convenience accessor for modules that need the vanilla client. */
	public static Minecraft client() {
		return Minecraft.getMinecraft();
	}
}
