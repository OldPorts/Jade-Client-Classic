package com.jadeclient.cosmetics;

import com.jadeclient.core.Category;
import com.jadeclient.core.Module;
import com.jadeclient.core.setting.BooleanSetting;
import com.jadeclient.core.setting.EnumSetting;

/**
 * Cosmetics settings: cape/wing style, kill effects (client-render only),
 * emotes (client-visible), hit particles. Everything is opt-in and local.
 */
public class CosmeticSettingsModule extends Module {

	public final EnumSetting cape = new EnumSetting("cape", "None",
			java.util.Arrays.asList("None", "Jade Classic", "Jade Facet", "Neon Vein"));
	public final EnumSetting wings = new EnumSetting("wings", "None",
			java.util.Arrays.asList("None", "Jade Wings", "Ember Wings"));
	public final EnumSetting bandana = new EnumSetting("bandana", "None",
			java.util.Arrays.asList("None", "Jade", "Crimson", "Azure"));
	public final EnumSetting hat = new EnumSetting("hat", "None",
			java.util.Arrays.asList("None", "Top Hat", "Halo", "Headphones"));
	public final EnumSetting killEffect = new EnumSetting("kill_effect", "None",
			java.util.Arrays.asList("None", "Jade Shatter", "Emerald Burst", "Snowflakes"));
	public final BooleanSetting emotes = new BooleanSetting("emotes", true);
	public final BooleanSetting mutualOptIn = new BooleanSetting("mutual_opt_in", true);

	public CosmeticSettingsModule() {
		super("cosmetics", "Cosmetics", Category.COSMETICS, "jade.module.cosmetics.desc");
		register(cape, wings, bandana, hat, killEffect, emotes, mutualOptIn);
	}

	@Override
	protected void onEnable() {
		CosmeticsManager.get().setSelfOptIn(true);
	}

	@Override
	protected void onDisable() {
		CosmeticsManager.get().setSelfOptIn(false);
	}
}
