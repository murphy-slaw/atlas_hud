package net.funkpla.atlas_hud;

import static net.funkpla.atlas_hud.AtlasHudMod.MOD_ID;

import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.resources.ResourceLocation;

public class AtlasHudClient implements ClientModInitializer {
  private static WorldLandmarks LANDMARKS;

  public static WorldLandmarks getLandmarks() {
    return LANDMARKS;
  }

  @Override
  public void onInitializeClient() {

    WorldSummary.enableLandmarks();

    HudRenderCallback.EVENT.register(new CompassHudOverlay());

    SurveyorClientEvents.Register.worldLoad(
        new ResourceLocation(MOD_ID, "world_load"),
        (level, summary, player, terrain, structures, landmarks) ->
            LANDMARKS = summary.landmarks());

    SurveyorClientEvents.Register.landmarksAdded(
        new ResourceLocation(MOD_ID, "landmarks_added"),
        (level, worldLandmarks, landmarks) -> LANDMARKS = worldLandmarks);

    SurveyorClientEvents.Register.landmarksRemoved(
        new ResourceLocation(MOD_ID, "landmarks_removed"),
        (world, worldLandmarks, landmarks) -> LANDMARKS = worldLandmarks);
  }
}
