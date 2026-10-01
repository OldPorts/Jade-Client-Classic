package com.jadeclient.forge;

import com.jadeclient.JadeClient;
import com.jadeclient.config.JadeConfig;
import com.jadeclient.core.Module;
import com.jadeclient.core.ModuleManager;
import com.jadeclient.core.event.EventBridge;
import com.jadeclient.core.perf.FpsTracker;
import com.jadeclient.modules.hud.KeystrokesModule;
import com.jadeclient.modules.qol.MacrosModule;
import com.jadeclient.modules.visual.FullbrightModule;
import com.jadeclient.modules.visual.HitMarkersModule;
import com.jadeclient.modules.visual.TimeChangerModule;
import com.jadeclient.modules.visual.ZoomModule;
import com.jadeclient.perf.JadePerfBridge;
import com.jadeclient.perf.PerformanceAccess;
import com.jadeclient.perf.PerformanceModule;
import com.jadeclient.ui.ClickGuiScreen;
import com.jadeclient.ui.OnboardingScreen;
import com.jadeclient.ui.ToastManager;
import com.jadeclient.ui.hud.HudEditorScreen;
import com.jadeclient.ui.hud.HudRenderDispatcher;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityRainFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Central Forge 1.8.9 event hub. Replaces the Fabric lifecycle hooks and the
 * SpongePowered mixins from the original build:
 *
 * <ul>
 *   <li>client tick: module ticks, key handling, onboarding, visual modules</li>
 *   <li>render tick: FPS tracking + unfocused FPS cap (was MinecraftMixin)</li>
 *   <li>overlay Pre/POST: HUD render + custom crosshair</li>
 *   <li>RenderLivingEvent.Pre: entity render distance (was EntityRendererMixin)</li>
 *   <li>EntityJoinWorldEvent: particle limiter (was ParticleEngineMixin)</li>
 *   <li>FOVUpdateEvent: smooth zoom</li>
 *   <li>AttackEntityEvent: hit markers</li>
 * </ul>
 */
@SideOnly(Side.CLIENT)
public final class JadeForgeEvents {

	private boolean rshiftWasDown;
	private long rshiftPressTime;
	private int tickCount;

	private float savedGamma = -1f;
	private boolean savedSmoothCamera;

	private final Deque<Long> lmbClicks = new ArrayDeque<Long>();
	private final Deque<Long> rmbClicks = new ArrayDeque<Long>();
	private boolean prevLmbDown;
	private boolean prevRmbDown;

	private final java.util.Random random = new java.util.Random();

	// -- Client tick -------------------------------------------------------

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (JadeClient.modules() == null) {
			return;
		}
		tickCount++;

		EventBridge.postTick();

		// First-launch onboarding once the main menu is up (no player yet).
		if (tickCount % 20 == 0
			&& mc.currentScreen instanceof GuiMainMenu
			&& OnboardingScreen.shouldShow()) {
			mc.displayGuiScreen(new OnboardingScreen());
		}

		if (mc.thePlayer == null || mc.theWorld == null) {
			return;
		}

		handleMenuKey(mc);
		pollKeystrokes(mc);
		applyFullbright(mc);
		applyTimeChanger(mc);
	}

	// -- Render tick: FPS stats + unfocused cap -----------------------------

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		FpsTracker.get().onFrame(PerformanceAccess.lastFrameDeltaMs());

		PerformanceModule perf = JadePerfBridge.module();
		if (perf == null || !perf.isEnabled()) {
			return;
		}
		if (Display.isActive()) {
			return;
		}
		float cap = perf.unfocusedFpsCap.get();
		if (cap < 5f) {
			return;
		}
		long frameNs = (long) (1000000000f / cap);
		long elapsed = System.nanoTime() - PerformanceAccess.lastFrameStart();
		long sleepMs = (frameNs - elapsed) / 1000000L;
		if (sleepMs > 0 && sleepMs < 200) {
			try {
				Thread.sleep(sleepMs);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}
	}

	// -- Key presses (module binds + chat macros) ---------------------------

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (!Keyboard.getEventKeyState()) {
			return;
		}
		int key = Keyboard.getEventKey();
		if (key == Keyboard.KEY_NONE) {
			return;
		}
		ModuleManager manager = JadeClient.modules();
		if (manager == null) {
			return;
		}
		if (manager.handleKeybind(key)) {
			JadeConfig.get().save();
		}
		Module macros = manager.byId("macros");
		if (macros instanceof MacrosModule) {
			((MacrosModule) macros).sendMacro(key);
		}
	}

	// -- HUD ----------------------------------------------------------------

	@SubscribeEvent
	public void onOverlayPre(RenderGameOverlayEvent.Pre event) {
		if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
			return;
		}
		if (com.jadeclient.cosmetics.JadeCosmeticsClient.shouldDrawCustomCrosshair()) {
			event.setCanceled(true);
			com.jadeclient.cosmetics.CrosshairRenderer.render(
				event.resolution.getScaledWidth() / 2,
				event.resolution.getScaledHeight() / 2);
		}
	}

	@SubscribeEvent
	public void onOverlayPost(RenderGameOverlayEvent.Post event) {
		if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null || mc.theWorld == null) {
			return;
		}
		if (mc.gameSettings.hideGUI) {
			return;
		}
		if (mc.currentScreen instanceof ClickGuiScreen
			|| mc.currentScreen instanceof HudEditorScreen
			|| mc.currentScreen instanceof OnboardingScreen) {
			return;
		}
		HudRenderDispatcher.renderAll();
		ToastManager.render();
	}

	// -- Perf: entity render distance ----------------------------------------

	@SubscribeEvent
	public void onRenderLiving(RenderLivingEvent.Pre event) {
		PerformanceModule perf = JadePerfBridge.module();
		if (perf == null || !perf.isEnabled()) {
			return;
		}
		int dist = perf.entityRenderDistance.get();
		if (dist <= 0) {
			return;
		}
		EntityLivingBase entity = event.entity;
		Minecraft mc = Minecraft.getMinecraft();
		if (entity == null || mc.thePlayer == null || entity == mc.thePlayer) {
			return;
		}
		if (entity.ridingEntity == mc.thePlayer || mc.thePlayer.ridingEntity == entity) {
			return;
		}
		if (entity.getDistanceSqToEntity(mc.thePlayer) > (double) dist * dist) {
			event.setCanceled(true);
		}
	}

	// -- Perf: particle limiter ----------------------------------------------

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.world == null || !event.world.isRemote) {
			return;
		}
		if (!(event.entity instanceof EntityFX)) {
			return;
		}
		PerformanceModule perf = JadePerfBridge.module();
		if (perf == null || !perf.isEnabled()) {
			return;
		}
		if (perf.noRainSplashes.get() && event.entity instanceof EntityRainFX) {
			event.setCanceled(true);
			return;
		}
		if (!perf.limitParticles.get()) {
			return;
		}
		int percent = perf.particleMultiplier.get();
		if (percent >= 100) {
			return;
		}
		if (random.nextInt(100) >= percent) {
			event.setCanceled(true);
		}
	}

	// -- Zoom -----------------------------------------------------------------

	@SubscribeEvent
	public void onFovUpdate(FOVUpdateEvent event) {
		Module raw = JadeClient.byId("zoom");
		if (!(raw instanceof ZoomModule)) {
			return;
		}
		ZoomModule zoom = (ZoomModule) raw;
		Minecraft mc = Minecraft.getMinecraft();
		boolean wantZoom = zoom.isEnabled()
			&& mc.currentScreen == null
			&& mc.thePlayer != null
			&& Keyboard.isKeyDown(Keyboard.KEY_C);
		if (wantZoom) {
			if (!zoom.active) {
				savedSmoothCamera = mc.gameSettings.smoothCamera;
			}
			event.newfov = event.fov / Math.max(1.5f, zoom.zoomFactor.get());
			if (zoom.cinematic.get()) {
				mc.gameSettings.smoothCamera = true;
			}
			zoom.active = true;
		} else if (zoom.active) {
			mc.gameSettings.smoothCamera = savedSmoothCamera;
			zoom.active = false;
		}
	}

	// -- Hit markers -----------------------------------------------------------

	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (event.entityPlayer != mc.thePlayer) {
			return;
		}
		Module raw = JadeClient.byId("hit_markers");
		if (raw instanceof HitMarkersModule && raw.isEnabled()) {
			((HitMarkersModule) raw).onHit();
		}
	}

	// -- Helpers ----------------------------------------------------------------

	/** Right Shift opens the menu; double-tap opens the HUD editor. */
	private void handleMenuKey(Minecraft mc) {
		boolean down = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		boolean pressed = down && !rshiftWasDown;
		rshiftWasDown = down;
		if (!pressed) {
			return;
		}
		if (mc.currentScreen instanceof ClickGuiScreen
			|| mc.currentScreen instanceof HudEditorScreen) {
			return; // those screens close themselves
		}
		long now = System.currentTimeMillis();
		if (mc.currentScreen == null) {
			if (now - rshiftPressTime < 400) {
				mc.displayGuiScreen(new HudEditorScreen());
			} else {
				mc.displayGuiScreen(new ClickGuiScreen());
			}
			rshiftPressTime = now;
		} else {
			mc.displayGuiScreen(new ClickGuiScreen());
		}
	}

	/** Polls WASD + mouse state into the keystrokes HUD each tick. */
	private void pollKeystrokes(Minecraft mc) {
		Module raw = JadeClient.byId("hud_keystrokes");
		if (!(raw instanceof KeystrokesModule) || !raw.isEnabled()) {
			return;
		}
		KeystrokesModule keys = (KeystrokesModule) raw;
		keys.w = Keyboard.isKeyDown(Keyboard.KEY_W);
		keys.a = Keyboard.isKeyDown(Keyboard.KEY_A);
		keys.s = Keyboard.isKeyDown(Keyboard.KEY_S);
		keys.d = Keyboard.isKeyDown(Keyboard.KEY_D);
		boolean lmb = Mouse.isButtonDown(0);
		boolean rmb = Mouse.isButtonDown(1);
		keys.lmb = lmb;
		keys.rmb = rmb;
		long now = System.currentTimeMillis();
		if (lmb && !prevLmbDown) {
			lmbClicks.addLast(now);
		}
		if (rmb && !prevRmbDown) {
			rmbClicks.addLast(now);
		}
		prevLmbDown = lmb;
		prevRmbDown = rmb;
		while (!lmbClicks.isEmpty() && now - lmbClicks.getFirst() > 1000) {
			lmbClicks.removeFirst();
		}
		while (!rmbClicks.isEmpty() && now - rmbClicks.getFirst() > 1000) {
			rmbClicks.removeFirst();
		}
		keys.cpsL = lmbClicks.size();
		keys.cpsR = rmbClicks.size();
	}

	private void applyFullbright(Minecraft mc) {
		Module raw = JadeClient.byId("fullbright");
		if (raw instanceof FullbrightModule && raw.isEnabled()) {
			if (savedGamma < 0f) {
				savedGamma = mc.gameSettings.gammaSetting;
			}
			mc.gameSettings.gammaSetting = ((FullbrightModule) raw).gamma.get();
		} else if (savedGamma >= 0f) {
			mc.gameSettings.gammaSetting = savedGamma;
			savedGamma = -1f;
		}
	}

	/** Client-side time/weather visuals. Gameplay time on servers is untouched. */
	private void applyTimeChanger(Minecraft mc) {
		Module raw = JadeClient.byId("time_changer");
		if (!(raw instanceof TimeChangerModule) || !raw.isEnabled()) {
			return;
		}
		TimeChangerModule changer = (TimeChangerModule) raw;
		String time = changer.time.get();
		long target = -1;
		if ("Sunrise".equals(time)) {
			target = 0;
		} else if ("Noon".equals(time)) {
			target = 6000;
		} else if ("Sunset".equals(time)) {
			target = 12000;
		} else if ("Midnight".equals(time)) {
			target = 18000;
		}
		if (target >= 0 && mc.theWorld.getWorldTime() != target) {
			mc.theWorld.setWorldTime(target);
		}
		String weather = changer.weather.get();
		if ("Clear".equals(weather) && mc.theWorld.isRaining()) {
			mc.theWorld.getWorldInfo().setRaining(false);
		} else if ("Rain".equals(weather) && !mc.theWorld.isRaining()) {
			mc.theWorld.getWorldInfo().setRaining(true);
		}
	}

	/** Preset helper used by the onboarding flow. */
	public static void applyPreset(String preset) {
		Module raw = JadeClient.byId("fps_boost");
		if (raw instanceof PerformanceModule) {
			PerformanceModule perf = (PerformanceModule) raw;
			perf.applyPreset(preset);
			perf.setEnabled(true);
		}
	}
}
