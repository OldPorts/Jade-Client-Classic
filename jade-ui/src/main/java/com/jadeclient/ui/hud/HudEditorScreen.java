package com.jadeclient.ui.hud;

import com.jadeclient.JadeClient;
import com.jadeclient.core.Module;
import com.jadeclient.ui.ToastManager;
import com.jadeclient.ui.render.Draw;
import com.jadeclient.modules.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * Drag-and-drop HUD editor. Shows all enabled HUD elements with selection
 * outlines; drag to move (with snapping to other elements and screen
 * centers), scroll to scale the selection.
 */
public class HudEditorScreen extends GuiScreen {

	private HudModule dragging;
	private float dragOffX;
	private float dragOffY;

	private static final float SNAP = 6f;

	public HudEditorScreen() {
	}

	public static void open() {
		Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// translucent backdrop
		drawGradientRect(0, 0, width, height, 0x66000000, 0x66000000);

		// alignment guides for the currently dragged element
		if (dragging != null) {
			int cx = width / 2;
			int cy = height / 2;
			float elCx = dragging.x() + dragging.renderWidth() / 2f;
			float elCy = dragging.y() + dragging.renderHeight() / 2f;
			if (Math.abs(elCx - cx) < SNAP * 2) {
				Draw.rect(cx, 0, 1, height, 0x8800C896);
				dragging.setPos(cx - dragging.renderWidth() / 2f, dragging.y());
				dragging.setSnapped(true);
			}
			if (Math.abs(elCy - cy) < SNAP * 2) {
				Draw.rect(0, cy, width, 1, 0x8800C896);
				dragging.setPos(dragging.x(), cy - dragging.renderHeight() / 2f);
				dragging.setSnapped(true);
			}
		}

		HudRenderDispatcher.renderAll(true);

		drawCenteredString(fontRendererObj, "Drag to move - Scroll to scale - ESC to save & exit",
				width / 2, height - 14, 0xFFFFFFFF);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		HudModule hit = hudAt(mouseX, mouseY);
		if (hit != null) {
			dragging = hit;
			dragOffX = mouseX - hit.x();
			dragOffY = mouseY - hit.y();
			return;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
		if (dragging != null) {
			dragging.setSnapped(false);
			dragging.setPos(mouseX - dragOffX, mouseY - dragOffY);
			return;
		}
		super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (dragging != null) {
			dragging = null;
			com.jadeclient.config.JadeConfig.get().save();
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0) {
			Minecraft mc = Minecraft.getMinecraft();
			int mx = Mouse.getEventX() * width / mc.displayWidth;
			int my = height - Mouse.getEventY() * height / mc.displayHeight - 1;
			HudModule hit = hudAt(mx, my);
			if (hit != null) {
				hit.setScale(hit.scale() + (wheel > 0 ? 0.1f : -0.1f));
			}
		}
	}

	private HudModule hudAt(double mx, double my) {
		HudModule best = null;
		for (Module module : JadeClient.modules().modules()) {
			if (module instanceof HudModule && module.isEnabled()) {
				HudModule hud = (HudModule) module;
				float w = hud.renderWidth() * hud.scale();
				float h = hud.renderHeight() * hud.scale();
				if (mx >= hud.x() && mx <= hud.x() + w && my >= hud.y() && my <= hud.y() + h) {
					best = hud; // topmost wins
				}
			}
		}
		return best;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) { // ESC
			com.jadeclient.config.JadeConfig.get().save();
			ToastManager.info("HUD layout saved");
			Minecraft.getMinecraft().displayGuiScreen(null);
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
