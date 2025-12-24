package net.funkpla.dead_reckoning;

import com.mojang.blaze3d.platform.InputConstants;
import folk.sisby.surveyor.WorldSummary;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadReckoning implements ClientModInitializer {
  public static final String MOD_ID = "dead_reckoning";
  public static final String MOD_NAME = "Dead Reckoning";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final DeadReckoningConfig CONFIG = DeadReckoningConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", MOD_ID, DeadReckoningConfig.class);
  public static final TagKey<Item> COMMON_COMPASSES = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "compasses"));
  public static final TagKey<Item> COMPASSES = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(DeadReckoning.MOD_ID, "compasses"));

  @Getter @Setter private static boolean HUD_ENABLED = true;
  private static final KeyMapping TOGGLE_HUD_KEY = new KeyMapping(
    "key.dead_reckoning.toggle",
    InputConstants.Type.KEYSYM,
    GLFW.GLFW_KEY_PERIOD,
    "category.dead_reckoning.general");

  public static boolean isCompass(ItemStack stack) {
    Registry<Item> registry = Minecraft.getInstance().level.registryAccess().registry(Registries.ITEM).orElseThrow();
    return registry.getTag(COMPASSES).isPresent() ? stack.is(COMPASSES) : registry.getTag(COMMON_COMPASSES).isPresent() ? stack.is(COMMON_COMPASSES) : CONFIG.compasses.contains(registry.getKey(stack.getItem()).toString());
  }

  @Override
  public void onInitializeClient() {
    LOGGER.info("Loading {}...", MOD_NAME);

    WorldSummary.enableLandmarks();

    HudRenderCallback.EVENT.register(new CompassOverlay());

    KeyBindingHelper.registerKeyBinding(TOGGLE_HUD_KEY);

    ClientTickEvents.END_CLIENT_TICK.register(
      client -> {
        while (TOGGLE_HUD_KEY.consumeClick()) {
          if (client.player == null) return;
          client.player.displayClientMessage(
            Component.translatable(
              "dead_reckoning.toggled_hud_visibility", isHUD_ENABLED() ? "off" : "on"),
            true);
          setHUD_ENABLED(!isHUD_ENABLED());
        }
      });
  }
}
