package net.funkpla.atlas_hud;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtlasHudMod implements ModInitializer {
  public static final String MOD_ID = "atlas_hud";
  public static final String MOD_NAME = "Dead Reckoning";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static AtlasHudConfig getConfig() {
    return AutoConfig.getConfigHolder(AtlasHudConfig.class).getConfig();
  }

  @Override
  public void onInitialize() {
    LOGGER.info("Loading {} HUD config.", MOD_NAME);
    AutoConfig.register(AtlasHudConfig.class, JanksonConfigSerializer::new);
  }
}
