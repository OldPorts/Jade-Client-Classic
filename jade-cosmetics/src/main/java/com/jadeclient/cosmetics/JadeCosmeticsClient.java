package com.jadeclient.cosmetics;

import com.jadeclient.JadeClient;
import com.jadeclient.core.Module;
import com.jadeclient.modules.visual.CrosshairModule;
import net.minecraft.client.Minecraft;

/**
 * jade-cosmetics client helpers. The actual crosshair hook lives in
 * {@code com.jadeclient.forge.JadeForgeEvents}; this answers whether the
 * custom crosshair should be drawn right now.
 */
public final class JadeCosmeticsClient {

	private JadeCosmeticsClient() {
	}

	public static boolean shouldDrawCustomCrosshair() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null || mc.gameSettings.hideGUI) {
			return false;
		}
		// Custom crosshair only makes sense in first person.
		if (mc.gameSettings.thirdPersonView != 0) {
			return false;
		}
		Module raw = JadeClient.byId("crosshair");
		return raw instanceof CrosshairModule && raw.isEnabled();
	}
}
