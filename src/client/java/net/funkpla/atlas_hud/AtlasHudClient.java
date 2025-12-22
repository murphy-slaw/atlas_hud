package net.funkpla.atlas_hud;

import static net.funkpla.atlas_hud.AtlasHudMod.MOD_ID;

import com.mojang.blaze3d.platform.InputConstants;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class AtlasHudClient implements ClientModInitializer {
  @Getter @Setter private static boolean HUD_ENABLED = true;
  private static WorldLandmarks LANDMARKS;
  private static KeyMapping TOGGLE_HUD_KEY;

  public static WorldLandmarks getLandmarks() {
    return LANDMARKS;
  }

  @Override
  public void onInitializeClient() {

    WorldSummary.enableLandmarks();

    HudRenderCallback.EVENT.register(new CompassHudOverlay());

    SurveyorClientEvents.Register.worldLoad(
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "world_load"),
        (level, summary, player, terrain, structures, landmarks) ->
            LANDMARKS = summary.landmarks());

    SurveyorClientEvents.Register.landmarksAdded(
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "landmarks_added"),
        (level, worldLandmarks, landmarks) -> LANDMARKS = worldLandmarks);

    SurveyorClientEvents.Register.landmarksRemoved(
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "landmarks_removed"),
        (world, worldLandmarks, landmarks) -> LANDMARKS = worldLandmarks);

    TOGGLE_HUD_KEY =
        KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.atlas_hud.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PERIOD,
                "category.atlas_hud.general"));

    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          while (TOGGLE_HUD_KEY.consumeClick()) {
            assert client.player != null;

            client.player.displayClientMessage(
                Component.translatable(
                    "atlas_hud.toggled_hud_visibility", isHUD_ENABLED() ? "off" : "on"),
                true);
            setHUD_ENABLED(!isHUD_ENABLED());
          }
        });
  }
}
