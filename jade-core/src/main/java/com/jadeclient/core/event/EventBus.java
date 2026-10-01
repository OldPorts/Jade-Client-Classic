package com.jadeclient.core.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Minimal event bus. Jade only fires a handful of high-frequency events, so a
 * simple typed dispatcher beats pulling in a full bus library.
 */
public final class EventBus {

	private static final EventBus INSTANCE = new EventBus();

	public static EventBus get() {
		return INSTANCE;
	}

	private EventBus() {
	}

	private static final class Subscription<T> {
		final Class<T> type;
		final Consumer<T> handler;

		Subscription(Class<T> type, Consumer<T> handler) {
			this.type = type;
			this.handler = handler;
		}
	}

	private final List<Subscription<?>> subscriptions = new CopyOnWriteArrayList<>();

	public <T> void subscribe(Class<T> type, Consumer<T> handler) {
		subscriptions.add(new Subscription<>(type, handler));
	}

	public <T> void unsubscribe(Class<T> type, Consumer<T> handler) {
		subscriptions.removeIf(sub -> sub.type == type && sub.handler == handler);
	}

	@SuppressWarnings("unchecked")
	public <T> void post(T event) {
		for (Subscription<?> sub : subscriptions) {
			if (sub.type.isInstance(event)) {
				((Consumer<T>) sub.handler).accept(event);
			}
		}
	}

	// -- Jade events -------------------------------------------------------

	/** Fired once per client tick (client thread). */
	public static final class ClientTick {
	}

	/** Fired once per rendered frame; carries the measured FPS. */
	public static final class Frame {
		public final float fps;
		public final float frameTimeMs;

		public Frame(float fps, float frameTimeMs) {
			this.fps = fps;
			this.frameTimeMs = frameTimeMs;
		}
	}

	/** Fired when a module is enabled or disabled. */
	public static final class ModuleToggled {
		public final com.jadeclient.core.Module module;
		public final boolean enabled;

		public ModuleToggled(com.jadeclient.core.Module module, boolean enabled) {
			this.module = module;
			this.enabled = enabled;
		}
	}
}
