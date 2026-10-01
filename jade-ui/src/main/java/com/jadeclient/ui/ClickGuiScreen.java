package com.jadeclient.ui;

import com.jadeclient.JadeClient;
import com.jadeclient.core.Category;
import com.jadeclient.core.Module;
import com.jadeclient.core.setting.BooleanSetting;
import com.jadeclient.core.setting.EnumSetting;
import com.jadeclient.core.setting.FloatSetting;
import com.jadeclient.core.setting.IntSetting;
import com.jadeclient.core.setting.Setting;
import com.jadeclient.core.setting.TextSetting;
import com.jadeclient.core.util.FuzzyMatcher;
import com.jadeclient.modules.hud.HudModule;
import com.jadeclient.ui.render.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Jade ClickGUI: a centered, glassy, three-pane screen in the Lunar /
 * Feather style.
 *
 *   [ categories ]   [ module list ]   [ settings panel ]
 *
 * Opened with Right Shift (default). ESC closes. Search bar filters modules
 * with fuzzy matching. The jade shimmer runs along the top edge.
 */
public class ClickGuiScreen extends GuiScreen {

	private static final int OPEN_ANIM_MS = 170;

	private Category selectedCategory = Category.PERFORMANCE;
	private Module selectedModule;
	private String search = "";

	// Per-module animated toggle position (0..1), updated each frame.
	private final Map<Object, Float> toggleAnim = new HashMap<Object, Float>();
	private long openTime;

	// Scroll offsets per pane
	private float moduleScroll;
	private float settingsScroll;

	// Hover tracking for soft highlight
	private int hoverModuleIdx = -1;

	public ClickGuiScreen() {
	}

	public static void open() {
		Minecraft.getMinecraft().displayGuiScreen(new ClickGuiScreen());
	}

	@Override
	public void initGui() {
		openTime = System.currentTimeMillis();
		moduleScroll = 0;
		settingsScroll = 0;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int w = width;
		int h = height;

		// Dim backdrop (stands in for glassmorphism blur at every GUI scale)
		drawGradientRect(0, 0, w, h, 0x88000000, 0x88000000);
		// Panel geometry: centered 480x260 GUI units
		int pw = Math.min(480, w - 40);
		int ph = Math.min(260, h - 40);
		int px = (w - pw) / 2;
		int py = (h - ph) / 2;

		float openProgress = Theme.ease((System.currentTimeMillis() - openTime) / (float) OPEN_ANIM_MS);

		// Panel background
		Draw.roundedRect(px, py, pw, ph, 8, Theme.bg);
		Draw.roundedRect(px, py, pw, ph, 8, (int) (openProgress * 255) << 24 | 0x161A22 & 0x00FFFFFF);

		// Jade shimmer along the top edge
		renderShimmer(px, py, pw, openProgress);

		// Title
		fontRendererObj.drawString("Jade Client", px + 12, py + 8, Theme.accent);
		String version = JadeClient.VERSION;
		fontRendererObj.drawString(version, px + pw - fontRendererObj.getStringWidth(version) - 12, py + 8,
				Theme.textDim);

		// Search bar (top center)
		int searchW = 140;
		int searchX = px + (pw - searchW) / 2;
		Draw.roundedRect(searchX, py + 6, searchW, 14, 4, Theme.bgCard);
		fontRendererObj.drawString(search.isEmpty() ? "Search..." : search, searchX + 6, py + 10, Theme.textDim);

		// Panes
		int paneTop = py + 26;
		int paneBottom = py + ph - 8;

		renderCategories(px + 8, paneTop, 96, paneBottom - paneTop, mouseX, mouseY);
		renderModuleList(px + 112, paneTop, 150, paneBottom - paneTop, mouseX, mouseY);
		renderSettings(px + 270, paneTop, pw - 278, paneBottom - paneTop, mouseX, mouseY);
	}

	private void renderShimmer(int px, int py, int pw, float open) {
		long t = System.currentTimeMillis();
		if (Theme.reducedMotion) {
			Draw.rect(px + 8, py, pw - 16, 2, Theme.accent);
			return;
		}
		// Moving highlight across a static accent line.
		Draw.rect(px + 8, py, pw - 16, 2, Theme.withAlpha(Theme.accent, 90));
		int sweep = (int) ((t / 12) % (pw - 16 + 120)) - 60;
		int sw = 90;
		int sx = px + 8 + sweep;
		int x0 = Math.max(px + 8, sx);
		int x1 = Math.min(px + pw - 8, sx + sw);
		if (x1 > x0) {
			Draw.rect(x0, py, x1 - x0, 2, Theme.accent);
		}
	}

	private void renderCategories(int x, int y, int w, int h, int mx, int my) {
		Draw.rect(x + w, y, 1, h, Theme.border);
		Category[] cats = Category.values();
		int rowH = 26;
		for (int i = 0; i < cats.length; i++) {
			Category cat = cats[i];
			int cy = y + i * rowH + 4;
			boolean selected = cat == selectedCategory;
			boolean hovered = mx >= x && mx < x + w && my >= cy && my < cy + rowH - 6;

			if (selected) {
				Draw.roundedRect(x, cy, w - 6, rowH - 6, 4, Theme.withAlpha(Theme.accent, 40));
				Draw.rect(x, cy + 3, 2, rowH - 12, Theme.accent);
			} else if (hovered) {
				Draw.roundedRect(x, cy, w - 6, rowH - 6, 4, Theme.withAlpha(Theme.text, 15));
			}
			int textX = x + 10;
			int color = selected ? Theme.accent : hovered ? Theme.text : Theme.textDim;
			fontRendererObj.drawString(cat.displayName(), textX, cy + (rowH - 12 - fontRendererObj.FONT_HEIGHT) / 2 + 3, color);
		}
	}

	private void renderModuleList(int x, int y, int w, int h, int mx, int my) {
		Draw.rect(x + w, y, 1, h, Theme.border);

		List<Module> modules = visibleModules();
		int rowH = 24;
		int maxRows = h / rowH;
		moduleScroll = Math.max(0, Math.min(modules.size() - maxRows, moduleScroll));

		enableScissor(x, y, w, h);
		for (int i = 0; i < modules.size(); i++) {
			Module m = modules.get(i);
			int cy = y + (int) ((i - moduleScroll) * rowH) + 2;
			if (cy + rowH < y || cy > y + h) {
				continue;
			}
			boolean hovered = mx >= x && mx < x + w && my >= cy && my < cy + rowH - 4;
			if (hovered) {
				hoverModuleIdx = i;
				Draw.roundedRect(x + 2, cy, w - 10, rowH - 4, 4, Theme.withAlpha(Theme.text, 12));
			}
			if (selectedModule == m) {
				Draw.roundedRect(x + 2, cy, w - 10, rowH - 4, 4, Theme.withAlpha(Theme.accent, 28));
			}

			// Name
			int nameColor = m.isEnabled() ? Theme.accent : Theme.text;
			fontRendererObj.drawString(m.name(), x + 8, cy + 7, nameColor);

			// Toggle pill (animated)
			Float animBox = toggleAnim.get(m);
			float anim = animBox == null ? (m.isEnabled() ? 1f : 0f) : animBox.floatValue();
			float target = m.isEnabled() ? 1f : 0f;
			anim += (target - anim) * 0.25f;
			toggleAnim.put(m, Float.valueOf(anim));

			int pillW = 22;
			int pillX = x + w - 8 - pillW;
			int pillY = cy + (rowH - 4 - 10) / 2;
			int pillBg = blend(Theme.bgCard, Theme.accent, anim);
			Draw.roundedRect(pillX, pillY, pillW, 10, 5, pillBg);
			int knobX = pillX + 2 + (int) (anim * (pillW - 10));
			Draw.roundedRect(knobX, pillY + 2, 6, 6, 3, 0xFFFFFFFF);
		}
		disableScissor();
	}

	private int blend(int from, int to, float t) {
		int a0 = (from >>> 24), r0 = (from >> 16) & 0xFF, g0 = (from >> 8) & 0xFF, b0 = from & 0xFF;
		int a1 = (to >>> 24), r1 = (to >> 16) & 0xFF, g1 = (to >> 8) & 0xFF, b1 = to & 0xFF;
		int a = (int) (a0 + (a1 - a0) * t);
		int r = (int) (r0 + (r1 - r0) * t);
		int gg = (int) (g0 + (g1 - g0) * t);
		int b = (int) (b0 + (b1 - b0) * t);
		return (a << 24) | (r << 16) | (gg << 8) | b;
	}

	private void renderSettings(int x, int y, int w, int h, int mx, int my) {
		if (selectedModule == null) {
			drawCenteredString(fontRendererObj, "Select a module", x + w / 2, y + h / 2 - 4, Theme.textDim);
			return;
		}
		fontRendererObj.drawString(selectedModule.name(), x + 4, y + 2, Theme.text);
		fontRendererObj.drawString(describe(selectedModule), x + 4, y + 14, Theme.textDim);

		int cy = y + 30;
		for (Setting<?> setting : selectedModule.settings()) {
			if (cy > y + h - 12) {
				break;
			}
			if (setting instanceof BooleanSetting) {
				BooleanSetting b = (BooleanSetting) setting;
				fontRendererObj.drawString(prettify(setting.id()), x + 4, cy, Theme.text);
				int pillX = x + w - 30;
				SettingAnimKey key = new SettingAnimKey("s:" + setting.id());
				Float animBox = toggleAnim.get(key);
				float anim = animBox == null ? (b.get() ? 1f : 0f) : animBox.floatValue();
				float target = b.get() ? 1f : 0f;
				anim += (target - anim) * 0.25f;
				toggleAnim.put(key, Float.valueOf(anim));
				Draw.roundedRect(pillX, cy - 1, 22, 10, 5, blend(Theme.bgCard, Theme.accent, anim));
				int knobX = pillX + 2 + (int) (anim * 12);
				Draw.roundedRect(knobX, cy + 1, 6, 6, 3, 0xFFFFFFFF);
				cy += 18;
			} else if (setting instanceof FloatSetting) {
				FloatSetting f = (FloatSetting) setting;
				fontRendererObj.drawString(prettify(setting.id()) + ": " + String.format("%.1f", f.get()), x + 4, cy,
						Theme.text);
				renderSlider(x + 4, cy + 10, w - 8, f.get(), f.min(), f.max());
				cy += 26;
			} else if (setting instanceof IntSetting) {
				IntSetting in = (IntSetting) setting;
				fontRendererObj.drawString(prettify(setting.id()) + ": " + in.get(), x + 4, cy, Theme.text);
				renderSlider(x + 4, cy + 10, w - 8, in.get(), in.min(), in.max());
				cy += 26;
			} else if (setting instanceof EnumSetting) {
				EnumSetting e = (EnumSetting) setting;
				fontRendererObj.drawString(prettify(setting.id()), x + 4, cy, Theme.text);
				String val = "< " + e.get() + " >";
				fontRendererObj.drawString(val, x + w - 8 - fontRendererObj.getStringWidth(val), cy,
						Theme.accent);
				cy += 18;
			} else if (setting instanceof TextSetting) {
				TextSetting t = (TextSetting) setting;
				fontRendererObj.drawString(prettify(setting.id()), x + 4, cy, Theme.text);
				fontRendererObj.drawString(t.get().isEmpty() ? "(empty)" : t.get(), x + 4, cy + 10, Theme.textDim);
				cy += 22;
			}
		}
	}

	private final Map<String, Float> sliderDrag = new HashMap<String, Float>();

	private void renderSlider(int x, int y, int w, double value, double min, double max) {
		float t = (float) ((value - min) / (max - min));
		Draw.roundedRect(x, y, w, 4, 2, Theme.bgCard);
		int fill = (int) (w * Math.max(0f, Math.min(1f, t)));
		Draw.roundedRect(x, y, fill, 4, 2, Theme.accent);
		Draw.roundedRect(x + fill - 3, y - 2, 6, 8, 3, 0xFFFFFFFF);
		// store slider geometry for click handling
		sliderDrag.put("x", Float.valueOf(x));
		sliderDrag.put("y", Float.valueOf(y));
		sliderDrag.put("w", Float.valueOf(w));
	}

	private static String describe(Module module) {
		String key = module.descriptionKey();
		String localized = StatCollector.translateToLocal(key);
		return localized == null || localized.isEmpty() ? key : localized;
	}

	private String prettify(String id) {
		String[] parts = id.split("_");
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
		}
		return sb.toString();
	}

	private List<Module> visibleModules() {
		List<Module> out = new ArrayList<Module>();
		if (!search.isEmpty()) {
			for (Module m : JadeClient.modules().modules()) {
				if (FuzzyMatcher.score(search, m.name()) >= 0) {
					out.add(m);
				}
			}
		} else {
			out.addAll(JadeClient.modules().byCategory(selectedCategory));
		}
		return out;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		int pw = Math.min(480, width - 40);
		int ph = Math.min(260, height - 40);
		int px = (width - pw) / 2;
		int py = (height - ph) / 2;

		// Click outside the panel closes it (Lunar behavior).
		if (mouseX < px || mouseX > px + pw || mouseY < py || mouseY > py + ph) {
			close();
			return;
		}

		int paneTop = py + 26;

		// Category pane clicks
		int catX = px + 8;
		if (mouseX >= catX && mouseX < catX + 96 && mouseY >= paneTop) {
			int idx = (mouseY - paneTop - 4) / 26;
			Category[] cats = Category.values();
			if (idx >= 0 && idx < cats.length) {
				selectedCategory = cats[idx];
				selectedModule = null;
				search = "";
				return;
			}
		}

		// Module pane clicks
		int modX = px + 112;
		if (mouseX >= modX && mouseX < modX + 150 && mouseY >= paneTop) {
			List<Module> modules = visibleModules();
			int idx = (int) ((mouseY - paneTop - 2) / 24 + moduleScroll);
			if (idx >= 0 && idx < modules.size()) {
				Module m = modules.get(idx);
				double relX = mouseX - (modX + 150 - 30);
				if (relX > 0) {
					m.toggle(); // clicked the pill
					ToastManager.info(m.name() + " " + (m.isEnabled() ? "enabled" : "disabled"));
					com.jadeclient.config.JadeConfig.get().save();
				} else {
					selectedModule = m;
				}
				return;
			}
		}

		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
		// Slider dragging in the settings pane
		if (selectedModule != null && sliderDrag.containsKey("x") && button == 0) {
			float sx = sliderDrag.get("x").floatValue();
			float sy = sliderDrag.get("y").floatValue();
			float sw = sliderDrag.get("w").floatValue();
			if (mouseY >= sy - 4 && mouseY <= sy + 8 && mouseX >= sx && mouseX <= sx + sw) {
				float t = (mouseX - sx) / sw;
				for (Setting<?> setting : selectedModule.settings()) {
					if (setting instanceof FloatSetting) {
						FloatSetting f = (FloatSetting) setting;
						f.set((float) (f.min() + t * (f.max() - f.min())));
					} else if (setting instanceof IntSetting) {
						IntSetting in = (IntSetting) setting;
						in.set((int) Math.round(in.min() + t * (in.max() - in.min())));
					}
				}
				return;
			}
		}
		super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			close();
			return;
		}
		if (keyCode == Keyboard.KEY_BACK) {
			if (!search.isEmpty()) {
				search = search.substring(0, search.length() - 1);
			}
			return;
		}
		// Typing into search
		if (typedChar >= 32 && typedChar < 127) {
			if (Character.isLetterOrDigit(typedChar) || typedChar == ' ' || typedChar == '_' || typedChar == '-') {
				search += typedChar;
				return;
			}
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void onGuiClosed() {
		com.jadeclient.config.JadeConfig.get().save();
		super.onGuiClosed();
	}

	private void close() {
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	private void enableScissor(int x, int y, int w, int h) {
		Minecraft mc = Minecraft.getMinecraft();
		int scale = new ScaledResolution(mc).getScaleFactor();
		int sx = x * scale;
		int sy = mc.displayHeight - (y + h) * scale;
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(sx, sy, w * scale, h * scale);
	}

	private void disableScissor() {
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	/** Wrapper so setting toggles can share the animation map with modules. */
	private static final class SettingAnimKey {
		private final String id;

		SettingAnimKey(String id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof SettingAnimKey && ((SettingAnimKey) o).id.equals(id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}
