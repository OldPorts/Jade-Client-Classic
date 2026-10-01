package com.jadeclient.core.util;

/**
 * Color math and geometry helpers shared by HUD modules and the ClickGUI.
 * Drawing itself stays in jade-ui so jade-core never touches client render
 * classes.
 */
public final class RenderUtil {

	private RenderUtil() {
	}

	/**
	 * A rounded rectangle approximated with stacked rects (body + caps).
	 * Cheap, crisp at every GUI scale, and good enough for the flat Jade
	 * look without custom shaders. Implemented in jade-ui's drawing layer.
	 */
	public static int[] roundedRectInsets(int w, int h, int radius) {
		int cap = Math.min(radius, Math.min(w, h) / 2);
		return new int[]{cap, cap, w - cap, h - cap};
	}

	/** Linear interpolation between two ARGB colors. */
	public static int blend(int from, int to, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int a0 = (from >>> 24), r0 = (from >> 16) & 0xFF, g0 = (from >> 8) & 0xFF, b0 = from & 0xFF;
		int a1 = (to >>> 24), r1 = (to >> 16) & 0xFF, g1 = (to >> 8) & 0xFF, b1 = to & 0xFF;
		int a = (int) (a0 + (a1 - a0) * t);
		int r = (int) (r0 + (r1 - r0) * t);
		int g = (int) (g0 + (g1 - g0) * t);
		int b = (int) (b0 + (b1 - b0) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/** Sets the alpha channel of an ARGB color. */
	public static int withAlpha(int argb, int alpha) {
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}

	/** Alpha-animates a color from 0..1. */
	public static int fade(int argb, float t) {
		return withAlpha(argb, (int) (t * (argb >>> 24)));
	}
}
