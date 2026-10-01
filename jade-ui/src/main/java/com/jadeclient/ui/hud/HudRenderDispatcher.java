package com.jadeclient.ui.hud;

import com.jadeclient.JadeClient;
import com.jadeclient.core.Module;
import com.jadeclient.ui.render.Draw;
import com.jadeclient.modules.hud.ArmorHudModule;
import com.jadeclient.modules.hud.ClockHudModule;
import com.jadeclient.modules.hud.CompassHudModule;
import com.jadeclient.modules.hud.EffectsHudModule;
import com.jadeclient.modules.hud.FpsHudModule;
import com.jadeclient.modules.hud.HudModule;
import com.jadeclient.modules.hud.InfoHudModule;
import com.jadeclient.modules.hud.KeystrokesModule;
import com.jadeclient.modules.hud.WatermarkModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.world.biome.BiomeGenBase;
import org.lwjgl.opengl.GL11;

import java.util.Collection;

/**
 * Draws every enabled HudModule at its stored position/scale. One draw pass
 * per module; modules stay dumb data holders so this file is the only place
 * that knows how to paint them.
 */
public final class HudRenderDispatcher {

	private HudRenderDispatcher() {
	}

	public static void renderAll() {
		renderAll(false);
	}

	public static void renderAll(boolean editorMode) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null && !editorMode) {
			return; // nothing to show on menus
		}
		for (Module module : JadeClient.modules().modules()) {
			if (!(module instanceof HudModule) || !module.isEnabled()) {
				continue;
			}
			HudModule hud = (HudModule) module;
			GL11.glPushMatrix();
			GL11.glTranslatef(hud.x(), hud.y(), 0f);
			GL11.glScalef(hud.scale(), hud.scale(), 1f);
			render(mc, mc.fontRendererObj, hud, editorMode);
			GL11.glPopMatrix();
		}
	}

	private static void render(Minecraft mc, FontRenderer font, HudModule hud, boolean editorMode) {
		if (hud instanceof FpsHudModule) {
			drawFps(font, (FpsHudModule) hud);
		} else if (hud instanceof InfoHudModule) {
			drawInfo(font, (InfoHudModule) hud);
		} else if (hud instanceof KeystrokesModule) {
			drawKeystrokes(font, (KeystrokesModule) hud);
		} else if (hud instanceof ArmorHudModule) {
			drawArmor((ArmorHudModule) hud);
		} else if (hud instanceof EffectsHudModule) {
			drawEffects(font, (EffectsHudModule) hud);
		} else if (hud instanceof ClockHudModule) {
			drawClock(font, (ClockHudModule) hud);
		} else if (hud instanceof CompassHudModule) {
			drawCompass(font, (CompassHudModule) hud);
		} else if (hud instanceof WatermarkModule) {
			drawWatermark(font, (WatermarkModule) hud);
		}

		if (editorMode) {
			// selection box
			int w = hud.renderWidth();
			int h = hud.renderHeight();
			Draw.rect(0, 0, w, 1, 0xFF00C896);
			Draw.rect(0, h - 1, w, 1, 0xFF00C896);
			Draw.rect(0, 0, 1, h, 0xFF00C896);
			Draw.rect(w - 1, 0, 1, h, 0xFF00C896);
		}
	}

	private static void drawFps(FontRenderer font, FpsHudModule m) {
		float fps = m.fps();
		int color = m.fpsColor(fps);
		font.drawStringWithShadow((int) fps + " FPS", 0, 0, color);
		int y = 10;
		if (m.showLow1Pct.get()) {
			font.drawStringWithShadow("1% " + (int) m.low1(), 0, y, 0xFF9AA3B2);
			y += 10;
		}
		if (m.showGraph.get()) {
			float[] graph = m.graph();
			int gw = 84;
			int gh = 20;
			for (int i = 0; i < graph.length && i < gw; i++) {
				float ft = graph[i];
				float norm = Math.min(1f, ft / 50f); // 50ms = bottom
				int barH = Math.max(1, (int) (gh * (1f - norm)));
				int x = i;
				Draw.rect(x, y + gh - barH, 1, barH, 0x8800C896);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void drawInfo(FontRenderer font, InfoHudModule m) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if (player == null) {
			return;
		}
		int y = 0;
		if (m.coords.get()) {
			font.drawStringWithShadow(String.format("XYZ %.0f %.0f %.0f",
					player.posX, player.posY, player.posZ), 0, y, 0xFFFFFFFF);
			y += 10;
		}
		if (m.biome.get() && mc.theWorld != null) {
			BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(
					new BlockPos(player.posX, player.posY, player.posZ));
			String name = biome == null ? "?" : biome.biomeName;
			font.drawStringWithShadow("Biome " + name, 0, y, 0xFFFFFFFF);
			y += 10;
		}
		if (m.dimension.get() && mc.theWorld != null) {
			font.drawStringWithShadow(mc.theWorld.provider.getDimensionName(), 0, y, 0xFFFFFFFF);
			y += 10;
		}
		if (m.server.get()) {
			String ip;
			if (mc.isSingleplayer()) {
				ip = "Singleplayer";
			} else if (mc.getCurrentServerData() != null) {
				ip = mc.getCurrentServerData().serverIP;
			} else {
				ip = "";
			}
			if (!ip.isEmpty()) {
				font.drawStringWithShadow(ip, 0, y, 0xFFFFFFFF);
				y += 10;
			}
		}
		if (m.showPing() && !mc.isSingleplayer() && mc.getNetHandler() != null) {
			NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(player.getUniqueID());
			int ping = info == null ? 0 : info.getResponseTime();
			font.drawStringWithShadow(ping + " ms", 0, y, 0xFFFFFFFF);
			y += 10;
		}
		if (m.direction.get()) {
			float yaw = (player.rotationYaw % 360f + 360f) % 360f;
			String dir;
			if (yaw < 45 || yaw >= 315) {
				dir = "S";
			} else if (yaw < 135) {
				dir = "W";
			} else if (yaw < 225) {
				dir = "N";
			} else {
				dir = "E";
			}
			font.drawStringWithShadow(dir, 0, y, 0xFF00C896);
			y += 10;
		}
	}

	private static void drawKeystrokes(FontRenderer font, KeystrokesModule m) {
		int size = 18;
		int gap = 2;
		// W row
		drawKey(font, "W", size, gap * 2 + size, 0, m.w, 0xFFFFFFFF);
		// A S D row
		drawKey(font, "A", size, 0, size + gap, m.a, 0xFFFFFFFF);
		drawKey(font, "S", size, size + gap, size + gap, m.s, 0xFFFFFFFF);
		drawKey(font, "D", size, (size + gap) * 2, size + gap, m.d, 0xFFFFFFFF);
		if (m.showMouse.get()) {
			String l = m.showCps.get() ? "LMB " + m.cpsL : "LMB";
			String r = m.showCps.get() ? "RMB " + m.cpsR : "RMB";
			int half = (size * 3 + gap * 2) / 2 - gap / 2;
			drawKey(font, l, half, 0, (size + gap) * 2, m.lmb, 0xFFFFFFFF);
			drawKey(font, r, half, half + gap, (size + gap) * 2, m.rmb, 0xFFFFFFFF);
		}
	}

	private static void drawKey(FontRenderer font, String label, int size, int x, int y,
			boolean pressed, int argb) {
		int bg = pressed ? 0x8800C896 : 0x660E1116;
		Draw.roundedRect(x, y, size, size, 3, bg);
		int sw = font.getStringWidth(label);
		font.drawString(label, x + (size - sw) / 2, y + (size - font.FONT_HEIGHT) / 2 + 1,
				pressed ? 0xFFFFFFFF : argb);
	}

	private static void drawArmor(ArmorHudModule m) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player == null) {
			return;
		}
		// armorInventory: 3 = helmet, 2 = chestplate, 1 = leggings, 0 = boots.
		int[] order = {3, 2, 1, 0};
		for (int i = 0; i < order.length; i++) {
			ItemStack stack = player.inventory.armorInventory[order[i]];
			if (stack == null) {
				continue;
			}
			int x = m.horizontal() ? i * 20 : 0;
			int y = m.horizontal() ? 0 : i * 20;
			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, x, y);
			if (m.showDurability.get() && stack.isItemStackDamageable()) {
				float frac = 1f - (float) stack.getItemDamage() / stack.getMaxDamage();
				int barW = (int) (16 * frac);
				int color = frac > 0.5f ? 0xFF00C896 : frac > 0.25f ? 0xFFFFC857 : 0xFFFF6B6B;
				Draw.rect(x, y + 17, 16, 2, 0xFF222222);
				Draw.rect(x, y + 17, barW, 2, color);
			}
		}
	}

	private static void drawEffects(FontRenderer font, EffectsHudModule m) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player == null) {
			return;
		}
		int y = 0;
		Collection<?> effects = player.getActivePotionEffects();
		for (Object o : effects) {
			PotionEffect effect = (PotionEffect) o;
			font.drawStringWithShadow(shorten(effect) + " " + (effect.getDuration() / 20) + "s",
					0, y, 0xFFFFFFFF);
			y += 10;
		}
	}

	private static String shorten(PotionEffect effect) {
		Potion potion = Potion.potionTypes[effect.getPotionID()];
		if (potion == null) {
			return "?";
		}
		String name = StatCollector.translateToLocal(potion.getName());
		StringBuilder sb = new StringBuilder();
		for (String p : name.split(" ")) {
			if (!p.isEmpty()) {
				sb.append(Character.toUpperCase(p.charAt(0)));
			}
		}
		return sb.length() == 0 ? name : sb.toString();
	}

	private static void drawClock(FontRenderer font, ClockHudModule m) {
		String real = java.time.LocalTime.now().withSecond(0).withNano(0).toString();
		font.drawStringWithShadow(real, 0, 0, 0xFFFFFFFF);
		if (m.showSession.get()) {
			font.drawStringWithShadow(m.sessionTime(), 0, 10, 0xFF9AA3B2);
		}
	}

	private static void drawCompass(FontRenderer font, CompassHudModule m) {
		float yaw = m.yaw();
		String[] dirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
		for (int i = 0; i < dirs.length; i++) {
			float angle = i * 45f;
			float diff = ((angle - yaw + 540f) % 360f) - 180f;
			if (Math.abs(diff) < 60f) {
				int x = (int) (60 + diff * 1.0f);
				int alpha = (int) (255 * (1f - Math.abs(diff) / 60f));
				font.drawString(dirs[i], x, 4, (alpha << 24) | 0x00FFFFFF);
			}
		}
		Draw.rect(59, 0, 2, 3, 0xFF00C896);
	}

	private static void drawWatermark(FontRenderer font, WatermarkModule m) {
		String s = "Jade Client";
		if (m.showVersion.get()) {
			s += " " + JadeClient.VERSION;
		}
		font.drawStringWithShadow(s, 0, 0, 0xFF00C896);
		if (!m.customText.get().isEmpty()) {
			font.drawStringWithShadow(m.customText.get(), 0, 11, 0xFFFFFFFF);
		}
	}
}
