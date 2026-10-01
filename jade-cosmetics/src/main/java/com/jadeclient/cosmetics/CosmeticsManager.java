package com.jadeclient.cosmetics;

import com.jadeclient.JadeClient;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cosmetics state. Jade cosmetics are opt-in and client-side: two Jade
 * players see each other's cape/wings only if BOTH have cosmetics enabled.
 * Nothing here affects gameplay or is transmitted to non-Jade players.
 */
public final class CosmeticsManager {

	private static final CosmeticsManager INSTANCE = new CosmeticsManager();

	public static CosmeticsManager get() {
		return INSTANCE;
	}

	private CosmeticsManager() {
	}

	/** Self opt-in flag (mirrors the Cosmetics module toggle). */
	private boolean selfOptIn = true;

	/** Known Jade users -> whether they opted in to showing cosmetics. */
	private final ConcurrentHashMap<UUID, Boolean> remoteOptIns = new ConcurrentHashMap<>();

	public void setSelfOptIn(boolean optIn) {
		this.selfOptIn = optIn;
	}

	public boolean selfOptIn() {
		return selfOptIn;
	}

	/** Can I see cosmetics on this player? Requires mutual opt-in. */
	public boolean visibleTo(UUID other) {
		if (!selfOptIn) {
			return false;
		}
		Boolean theirs = remoteOptIns.get(other);
		return theirs != null && theirs;
	}

	public void recordRemoteOptIn(UUID player, boolean optedIn) {
		remoteOptIns.put(player, optedIn);
	}

	public void clear() {
		remoteOptIns.clear();
	}
}
