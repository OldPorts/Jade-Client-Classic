package com.jadeclient.ui.render;

import com.jadeclient.core.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * Client drawing helpers built on top of the mapping-free math in
 * {@link RenderUtil}. All Jade UI drawing funnels through here.
 * Forge 1.8.9 backend: {@link Gui#drawRect} + a Tessellator gradient.
 */
public final class Draw {

	private Draw() {
	}

	public static void rect(int x, int y, int w, int h, int argb) {
		Gui.drawRect(x, y, x + w, y + h, argb);
	}

	/**
	 * Rounded rectangle: center body plus side caps. Crisp at GUI
	 * scale 1-4, no shaders, no textures.
	 */
	public static void roundedRect(int x, int y, int w, int h, int radius, int argb) {
		int cap = Math.min(radius, Math.min(w, h) / 2);
		Gui.drawRect(x + cap, y, x + w - cap, y + h, argb);           // vertical body
		Gui.drawRect(x, y + cap, x + cap, y + h - cap, argb);         // left cap
		Gui.drawRect(x + w - cap, y + cap, x + w, y + h - cap, argb); // right cap
	}

	/** Soft glow via layered translucent expansion. */
	public static void glow(int x, int y, int w, int h, int argb, int layers) {
		int step = 2;
		for (int i = layers; i > 0; i--) {
			int expand = i * step;
			int a = Math.max(1, (argb >>> 24) / (i * 2));
			Gui.drawRect(x - expand, y - expand, x + w + expand, y + h + expand,
					(a << 24) | (argb & 0x00FFFFFF));
		}
	}

	/** Vertical ARGB gradient usable outside GuiScreen (HUD rendering). */
	public static void gradient(int x, int y, int w, int h, int topArgb, int bottomArgb) {
		float topA = (topArgb >> 24 & 255) / 255f;
		float topR = (topArgb >> 16 & 255) / 255f;
		float topG = (topArgb >> 8 & 255) / 255f;
		float topB = (topArgb & 255) / 255f;
		float botA = (bottomArgb >> 24 & 255) / 255f;
		float botR = (bottomArgb >> 16 & 255) / 255f;
		float botG = (bottomArgb >> 8 & 255) / 255f;
		float botB = (bottomArgb & 255) / 255f;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		Tessellator tess = Tessellator.getInstance();
		WorldRenderer buf = tess.getWorldRenderer();
		buf.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buf.pos(x + w, y, 0).color(topR, topG, topB, topA).endVertex();
		buf.pos(x, y, 0).color(topR, topG, topB, topA).endVertex();
		buf.pos(x, y + h, 0).color(botR, botG, botB, botA).endVertex();
		buf.pos(x + w, y + h, 0).color(botR, botG, botB, botA).endVertex();
		tess.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
	}

	public static int blend(int from, int to, float t) {
		return RenderUtil.blend(from, to, t);
	}

	public static int withAlpha(int argb, int alpha) {
		return RenderUtil.withAlpha(argb, alpha);
	}
}
