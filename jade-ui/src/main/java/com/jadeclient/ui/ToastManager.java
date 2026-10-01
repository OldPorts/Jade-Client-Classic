package com.jadeclient.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bottom-right toast notifications for toggles, config saves, errors.
 * Slides in, holds ~2s, fades out.
 */
public final class ToastManager {

	public static final class Toast {
		public final String text;
		public final long bornAt;
		public final int accent;

		public Toast(String text, long bornAt, int accent) {
			this.text = text;
			this.bornAt = bornAt;
			this.accent = accent;
		}
	}

	private static final List<Toast> active = new ArrayList<Toast>();
	private static final long LIFE_MS = 2200;

	public static void info(String message) {
		push(message, Theme.accent);
	}

	public static void error(String message) {
		push(message, 0xFFFF6B6B);
	}

	public static void push(String text, int accent) {
		active.add(new Toast(text, System.currentTimeMillis(), accent));
		if (active.size() > 5) {
			active.remove(0);
		}
	}

	public static void render() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null) {
			return;
		}
		long now = System.currentTimeMillis();
		for (Iterator<Toast> it = active.iterator(); it.hasNext();) {
			if (now - it.next().bornAt > LIFE_MS) {
				it.remove();
			}
		}

		ScaledResolution res = new ScaledResolution(mc);
		FontRenderer font = mc.fontRendererObj;
		int y = res.getScaledHeight() - 24;

		for (Toast toast : active) {
			float age = (now - toast.bornAt) / 1000f;
			float alpha;
			if (age < 0.15f) {
				alpha = age / 0.15f;
			} else if (age > 1.9f) {
				alpha = Math.max(0f, 1f - (age - 1.9f) / 0.3f);
			} else {
				alpha = 1f;
			}
			int a = (int) (alpha * 255) << 24;

			int width = font.getStringWidth(toast.text) + 16;
			int x = res.getScaledWidth() - width - 8;

			com.jadeclient.ui.render.Draw.roundedRect(x, y, width, 16, 4,
					a | (Theme.bgPanel & 0x00FFFFFF));
			com.jadeclient.ui.render.Draw.rect(x, y, 2, 16, a & toast.accent);
			font.drawStringWithShadow(toast.text, x + 8, y + 4, a | 0x00FFFFFF);
			y -= 20;
		}
	}

	public static void clear() {
		active.clear();
	}
}
