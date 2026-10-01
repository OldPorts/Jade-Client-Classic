package com.jadeclient.ui;

/**
 * Jade UI theme. Colors are ARGB ints; the accent defaults to jade green
 * and can be overridden by the accent picker in Settings.
 */
public final class Theme {

	public enum Mode {
		DARK, LIGHT, JADE
	}

	public static Mode mode = Mode.DARK;
	public static int accent = 0xFF00C896;
	public static float blurIntensity = 0.5f;
	public static float animationSpeed = 1.0f; // multiplier; lower = faster
	public static boolean reducedMotion = false;
	public static int colorblindShift = 0; // 0 none, 1 protan, 2 deutan, 3 tritan

	// Dark palette (default)
	public static int bg = 0xF00E1116;
	public static int bgPanel = 0xF0161A22;
	public static int bgCard = 0xF01B202B;
	public static int text = 0xFFF5F7FA;
	public static int textDim = 0xFF9AA3B2;
	public static int border = 0xFF232A36;

	public static void apply(Mode m) {
		mode = m;
		if (m == Mode.LIGHT) {
			bg = 0xF0F5F7FA;
			bgPanel = 0xF0FFFFFF;
			bgCard = 0xF0EDF1F5;
			text = 0xFF0E1116;
			textDim = 0xFF5A6472;
			border = 0xFFD6DCE4;
		} else if (m == Mode.JADE) {
			bg = 0xF00B1512;
			bgPanel = 0xF0101F1A;
			bgCard = 0xF0152822;
			text = 0xFFE8FFF6;
			textDim = 0xFF7FA89A;
			border = 0xFF1E3A30;
		} else {
			bg = 0xF00E1116;
			bgPanel = 0xF0161A22;
			bgCard = 0xF01B202B;
			text = 0xFFF5F7FA;
			textDim = 0xFF9AA3B2;
			border = 0xFF232A36;
		}
	}

	public static int withAlpha(int argb, int alpha) {
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}

	/** Eased 0..1 progress for a 160ms animation, scaled by animationSpeed. */
	public static float ease(float linear01) {
		float t = Math.max(0f, Math.min(1f, linear01));
		if (reducedMotion) {
			return t < 1f ? 0f : 1f; // snap, no motion
		}
		float speed = Math.max(0.25f, animationSpeed);
		float scaled = Math.min(1f, t / speed);
		// cubic ease-out
		float inv = 1f - scaled;
		return 1f - inv * inv * inv;
	}
}
