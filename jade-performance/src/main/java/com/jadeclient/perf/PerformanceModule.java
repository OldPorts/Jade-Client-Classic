package com.jadeclient.perf;

import com.jadeclient.core.Category;
import com.jadeclient.core.Module;
import com.jadeclient.core.setting.BooleanSetting;
import com.jadeclient.core.setting.EnumSetting;
import com.jadeclient.core.setting.FloatSetting;
import com.jadeclient.core.setting.IntSetting;

/**
 * Master FPS Boost. Central switchboard for every optimization; the perf
 * mixins read these settings directly so there is one source of truth.
 */
public class PerformanceModule extends Module {

	// -- Toggles -----------------------------------------------------------

	public final BooleanSetting entityCulling = new BooleanSetting("entity_culling", true);
	public final BooleanSetting blockEntityCulling = new BooleanSetting("block_entity_culling", true);
	public final BooleanSetting occlusionCulling = new BooleanSetting("occlusion_culling", true);
	public final BooleanSetting smartFrustum = new BooleanSetting("smart_frustum", true);
	public final BooleanSetting asyncChunkBuilding = new BooleanSetting("async_chunk_building", true);
	public final BooleanSetting batchedMeshing = new BooleanSetting("batched_meshing", true);
	public final BooleanSetting fastMath = new BooleanSetting("fast_math", true);
	public final BooleanSetting fastRender = new BooleanSetting("fast_render", true);
	public final BooleanSetting limitParticles = new BooleanSetting("limit_particles", true);
	public final BooleanSetting noRainSplashes = new BooleanSetting("no_rain_splashes", false);
	public final BooleanSetting lowFireSmoke = new BooleanSetting("low_fire_smoke", true);

	// -- Sliders -----------------------------------------------------------

	public final IntSetting particleMultiplier = new IntSetting("particle_density", 100, 0, 400);
	public final IntSetting chunkFadeSpeed = new IntSetting("chunk_fade_speed", 100, 0, 500);
	public final IntSetting entityRenderDistance = new IntSetting("entity_render_distance", 0, 0, 32);
	public final FloatSetting fpsCap = new FloatSetting("fps_cap", 0f, 0f, 500f); // 0 = uncapped
	public final FloatSetting unfocusedFpsCap = new FloatSetting("unfocused_fps_cap", 30f, 5f, 240f);

	// -- Presets -----------------------------------------------------------

	public final EnumSetting preset = new EnumSetting("preset", "Balanced",
			java.util.Arrays.asList("Low", "Balanced", "High", "Jade-Max"));

	public PerformanceModule() {
		super("fps_boost", "Master FPS Boost", Category.PERFORMANCE, "jade.module.fps_boost.desc");
		register(entityCulling, blockEntityCulling, occlusionCulling, smartFrustum,
				asyncChunkBuilding, batchedMeshing, fastMath, fastRender,
				limitParticles, noRainSplashes, lowFireSmoke,
				particleMultiplier, chunkFadeSpeed, entityRenderDistance, fpsCap, unfocusedFpsCap,
				preset);
	}

	/** Applies a named preset to every perf setting. Called from the UI. */
	public void applyPreset(String name) {
		if ("Low".equals(name)) {
			entityCulling.set(true);
			blockEntityCulling.set(true);
			occlusionCulling.set(true);
			smartFrustum.set(false);
			asyncChunkBuilding.set(true);
			batchedMeshing.set(true);
			fastMath.set(true);
			fastRender.set(true);
			limitParticles.set(true);
			particleMultiplier.set(30);
			chunkFadeSpeed.set(300);
			entityRenderDistance.set(16);
			fpsCap.set(0f);
			unfocusedFpsCap.set(20f);
		} else if ("Balanced".equals(name)) {
			entityCulling.set(true);
			blockEntityCulling.set(true);
			occlusionCulling.set(true);
			smartFrustum.set(true);
			asyncChunkBuilding.set(true);
			batchedMeshing.set(true);
			fastMath.set(true);
			fastRender.set(true);
			limitParticles.set(true);
			particleMultiplier.set(100);
			chunkFadeSpeed.set(150);
			entityRenderDistance.set(0);
			fpsCap.set(0f);
			unfocusedFpsCap.set(30f);
		} else if ("High".equals(name)) {
			entityCulling.set(true);
			blockEntityCulling.set(true);
			occlusionCulling.set(true);
			smartFrustum.set(true);
			asyncChunkBuilding.set(true);
			batchedMeshing.set(true);
			fastMath.set(false);
			fastRender.set(true);
			limitParticles.set(true);
			particleMultiplier.set(200);
			chunkFadeSpeed.set(100);
			entityRenderDistance.set(0);
			fpsCap.set(0f);
			unfocusedFpsCap.set(60f);
		} else if ("Jade-Max".equals(name)) {
			entityCulling.set(true);
			blockEntityCulling.set(true);
			occlusionCulling.set(true);
			smartFrustum.set(true);
			asyncChunkBuilding.set(true);
			batchedMeshing.set(true);
			fastMath.set(false);
			fastRender.set(false);
			limitParticles.set(false);
			particleMultiplier.set(400);
			chunkFadeSpeed.set(0);
			entityRenderDistance.set(0);
			fpsCap.set(0f);
			unfocusedFpsCap.set(240f);
		}
	}
}
