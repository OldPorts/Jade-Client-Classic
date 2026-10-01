package com.jadeclient.core.util;

/**
 * Lightweight fuzzy matcher used by the ClickGUI search bar.
 * Subsequence match with a score: consecutive hits and word-start hits rank
 * higher. Good enough to feel instant, cheap enough to run every keystroke.
 */
public final class FuzzyMatcher {

	private FuzzyMatcher() {
	}

	/** Returns a score &gt;= 0 if needle fuzzy-matches haystack, -1 otherwise. */
	public static int score(String needle, String haystack) {
		if (needle.isEmpty()) {
			return 0;
		}
		String n = needle.toLowerCase();
		String h = haystack.toLowerCase();

		int ni = 0;
		int score = 0;
		int lastHit = -2;

		for (int hi = 0; hi < h.length() && ni < n.length(); hi++) {
			if (h.charAt(hi) == n.charAt(ni)) {
				score += (hi == lastHit + 1) ? 3 : 1;      // consecutive bonus
				if (hi == 0 || h.charAt(hi - 1) == ' ' || h.charAt(hi - 1) == '_') {
					score += 2;                            // word-start bonus
				}
				lastHit = hi;
				ni++;
			}
		}
		return ni == n.length() ? score : -1;
	}
}
