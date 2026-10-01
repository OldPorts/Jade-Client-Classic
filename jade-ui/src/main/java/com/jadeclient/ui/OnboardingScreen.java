package com.jadeclient.ui;

import com.jadeclient.ui.render.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * First-launch onboarding: 3 steps - theme, performance preset, keybind
 * summary. Only shows once; writes a flag into the Jade config dir.
 */
public class OnboardingScreen extends GuiScreen {

	private int step;
	private Theme.Mode chosenTheme = Theme.Mode.DARK;
	private String chosenPreset = "Balanced";

	public OnboardingScreen() {
	}

	public static boolean shouldShow() {
		try {
			return !Files.exists(
					com.jadeclient.config.JadeConfig.get().root().resolve("onboarded"));
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawGradientRect(0, 0, width, height, 0xEE0E1116, 0xEE0E1116);

		int pw = 220;
		int ph = 130;
		int px = (width - pw) / 2;
		int py = (height - ph) / 2;

		Draw.roundedRect(px, py, pw, ph, 8, Theme.bgPanel);
		Draw.rect(px + 8, py, pw - 16, 2, Theme.accent);

		String title;
		if (step == 0) {
			title = "Pick your theme";
		} else if (step == 1) {
			title = "Choose a performance preset";
		} else {
			title = "You're all set!";
		}
		drawCenteredString(fontRendererObj, title, width / 2, py + 16, Theme.text);

		if (step == 0) {
			String[] themes = {"Dark", "Light", "Jade"};
			for (int i = 0; i < themes.length; i++) {
				int bx = px + 16 + i * 64;
				boolean sel = chosenTheme.ordinal() == i;
				Draw.roundedRect(bx, py + 40, 56, 22, 5,
						sel ? Theme.accent : Theme.bgCard);
				drawCenteredString(fontRendererObj, themes[i], bx + 28, py + 47,
						sel ? 0xFF0E1116 : Theme.text);
			}
		} else if (step == 1) {
			String[] presets = {"Low", "Balanced", "High", "Jade-Max"};
			for (int i = 0; i < presets.length; i++) {
				int bx = px + 12 + i * 50;
				boolean sel = chosenPreset.equals(presets[i]);
				Draw.roundedRect(bx, py + 40, 46, 22, 5,
						sel ? Theme.accent : Theme.bgCard);
				drawCenteredString(fontRendererObj, presets[i], bx + 23, py + 47,
						sel ? 0xFF0E1116 : Theme.text);
			}
		} else {
			drawCenteredString(fontRendererObj, "RShift - Open Jade menu", width / 2, py + 44, Theme.text);
			drawCenteredString(fontRendererObj, "RShift again - HUD editor", width / 2, py + 58, Theme.text);
			drawCenteredString(fontRendererObj, "C - Zoom", width / 2, py + 72, Theme.text);
			drawCenteredString(fontRendererObj, "Everything else lives in the menu.", width / 2, py + 90, Theme.textDim);
		}

		// Next / Finish button
		String btn = step < 2 ? "Next" : "Finish";
		Draw.roundedRect(px + pw - 56, py + ph - 26, 44, 16, 5, Theme.accent);
		drawCenteredString(fontRendererObj, btn, px + pw - 34, py + ph - 22, 0xFF0E1116);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		int pw = 220;
		int ph = 130;
		int px = (width - pw) / 2;
		int py = (height - ph) / 2;

		if (step == 0) {
			for (int i = 0; i < 3; i++) {
				int bx = px + 16 + i * 64;
				if (mouseX >= bx && mouseX <= bx + 56 && mouseY >= py + 40 && mouseY <= py + 62) {
					chosenTheme = Theme.Mode.values()[i];
					Theme.apply(chosenTheme);
					return;
				}
			}
		} else if (step == 1) {
			String[] presets = {"Low", "Balanced", "High", "Jade-Max"};
			for (int i = 0; i < presets.length; i++) {
				int bx = px + 12 + i * 50;
				if (mouseX >= bx && mouseX <= bx + 46 && mouseY >= py + 40 && mouseY <= py + 62) {
					chosenPreset = presets[i];
					JadeClientClient.applyPreset(chosenPreset);
					return;
				}
			}
		}

		// Next / Finish
		if (mouseX >= px + pw - 56 && mouseX <= px + pw - 12
				&& mouseY >= py + ph - 26 && mouseY <= py + ph - 10) {
			step++;
			if (step > 2) {
				finish();
			}
			return;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	private void finish() {
		try {
			Files.write(
					com.jadeclient.config.JadeConfig.get().root().resolve("onboarded"),
					"1".getBytes(StandardCharsets.UTF_8));
		} catch (Exception ignored) {
		}
		com.jadeclient.config.JadeConfig.get().save();
		ToastManager.info("Welcome to Jade Client");
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			finish();
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
