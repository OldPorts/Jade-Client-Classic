package com.jadeclient.cosmetics;

import com.jadeclient.JadeClient;
import com.jadeclient.core.Module;
import com.jadeclient.modules.visual.CrosshairModule;
import com.jadeclient.ui.render.Draw;

/**
 * Draws the custom crosshair when the Crosshair Editor module is enabled.
 * Purely decorative - replaces the vanilla sprite only.
 */
public final class CrosshairRenderer {

	private CrosshairRenderer() {
	}

	public static void render(int cx, int cy) {
		Module raw = JadeClient.byId("crosshair");
		if (!(raw instanceof CrosshairModule) || !raw.isEnabled()) {
			return;
		}
		CrosshairModule m = (CrosshairModule) raw;
		int size = m.size.get();
		int thick = m.thickness.get();
		int color;
		if (m.rainbow.get()) {
			color = 0xFF000000 | (int) (System.currentTimeMillis() / 8 % 0xFFFFFF);
		} else {
			color = m.color.get();
		}

		String style = m.style.get();
		if ("Dot".equals(style)) {
			Draw.roundedRect(cx - size / 2, cy - size / 2, size, size, 1, color);
		} else if ("Cross".equals(style)) {
			Draw.rect(cx - size, cy - thick / 2, size * 2, thick, color);
			Draw.rect(cx - thick / 2, cy - size, thick, size * 2, color);
		} else if ("Circle".equals(style)) {
			drawCircle(cx, cy, size, thick, color);
		} else if ("Circle Dot".equals(style)) {
			drawCircle(cx, cy, size, thick, color);
			Draw.roundedRect(cx - 1, cy - 1, 2, 2, 1, color);
		} else if ("Plus Gap".equals(style)) {
			int gap = 2;
			Draw.rect(cx - size - gap, cy - thick / 2, size, thick, color);
			Draw.rect(cx + gap, cy - thick / 2, size, thick, color);
			Draw.rect(cx - thick / 2, cy - size - gap, thick, size, color);
			Draw.rect(cx - thick / 2, cy + gap, thick, size, color);
		} else if ("T Shape".equals(style)) {
			Draw.rect(cx - size, cy - thick / 2, size * 2, thick, color);
			Draw.rect(cx - thick / 2, cy, thick, size, color);
		}
	}

	/** Pixel circle (approximation good enough at GUI scale 1-4). */
	private static void drawCircle(int cx, int cy, int r, int thick, int color) {
		for (int a = 0; a < 360; a += 6) {
			double rad = Math.toRadians(a);
			int x = cx + (int) Math.round(Math.cos(rad) * r);
			int y = cy + (int) Math.round(Math.sin(rad) * r);
			Draw.rect(x, y, thick, thick, color);
		}
	}
}
