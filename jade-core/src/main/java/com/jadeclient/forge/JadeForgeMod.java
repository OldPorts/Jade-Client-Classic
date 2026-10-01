package com.jadeclient.forge;

import com.jadeclient.JadeClient;
import com.jadeclient.perf.JadePerfBridge;
import com.jadeclient.perf.PerformanceInit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Forge 1.8.9 entrypoint for Jade Client.
 *
 * Single {@code @Mod} jar: every former Fabric submodule is merged into this
 * mod (see the root build.gradle sourceSet wiring).
 */
@Mod(
	modid = JadeClient.MOD_ID,
	name = "Jade Client",
	version = JadeClient.VERSION,
	acceptedMinecraftVersions = "[1.8.9]",
	acceptableRemoteVersions = "*"
)
public final class JadeForgeMod {

	@Mod.Instance(JadeClient.MOD_ID)
	public static JadeForgeMod instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// Config lives in .minecraft/jade/ (same layout as the Fabric build).
		com.jadeclient.config.ConfigInit.initEarly(
			event.getModConfigurationDirectory().getParentFile());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (event.getSide() != Side.CLIENT) {
			return;
		}
		JadeClient.boot(Minecraft.getMinecraft().mcDataDir);
		PerformanceInit.install(JadeClient.modules());
		MinecraftForge.EVENT_BUS.register(new JadeForgeEvents());
		JadeClient.LOGGER.info("Jade Client Forge hooks registered");
	}
}
