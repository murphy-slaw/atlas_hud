package net.funkpla.dead_reckoning;

import com.google.common.primitives.Ints;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.trinkets.TrinketsMain;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClient;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import java.util.*;
import java.util.stream.IntStream;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompassOverlay implements HudRenderCallback {
  private static final int ITEM_Z_OFFSET = 150;
  private static final int BASE_BOSSBAR_OFFSET = 12;
  private static final int BOSSBAR_HEIGHT = 19;
  private static final ResourceLocation DECORATION_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(DeadReckoning.MOD_ID, "textures/gui/decoration.png");
  private static final ResourceLocation DECORATION_LEFT_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(DeadReckoning.MOD_ID, "textures/gui/left.png");
  private static final ResourceLocation DECORATION_RIGHT_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(DeadReckoning.MOD_ID, "textures/gui/right.png");
  private static final int DECORATION_HEIGHT = 5;
  private final int WHITE = 0xFFFFFF;
  private Player player;
  private Font font;
  private TrinketComponent trinkets;
  @Getter private int centerX;
  @Getter private int compassWidth;
  @Getter private int compassStartX;
  @Getter private int compassEndX;
  private int bossYOffset;
  private GuiGraphics ctx;

  @Override
  public void onHudRender(GuiGraphics ctx, DeltaTracker deltaTracker) {
    Minecraft client = Minecraft.getInstance();
    this.player = client.player;

    if (FabricLoader.getInstance().isModLoaded(TrinketsMain.MOD_ID)) {
      TrinketsApi.getTrinketComponent(player).ifPresent(component -> this.trinkets = component);
    }

    font = client.font;
    this.ctx = ctx;
    int windowWidth = ctx.guiWidth();
    centerX = windowWidth / 2;
    compassWidth = (int) (windowWidth * (DeadReckoning.CONFIG.alignment.screenWidth));
    compassStartX = centerX - (compassWidth / 2);
    compassEndX = centerX + (compassWidth / 2);
    bossYOffset = getBossOffset(client);

    renderBackground();
    renderDirections();
    renderMarkers();
  }

  private int getBossOffset(Minecraft client) {
    BossHealthOverlay bossOverlay = client.gui.getBossOverlay();
    int yOffset = 0;
    if (!bossOverlay.events.isEmpty()) {
      yOffset = BASE_BOSSBAR_OFFSET + 2;
      for (int i = 1; i < bossOverlay.events.size(); i++) {
        yOffset += BOSSBAR_HEIGHT;
      }
    }
    return yOffset;
  }

  private boolean shouldShowCompass() {
    boolean result = false;
    if (DeadReckoning.isHUD_ENABLED()) {
      result =
          switch (DeadReckoning.CONFIG.displayMode) {
            case COMPASS_HELD -> isCompassHeld();
            case COMPASS_HOTBAR -> isCompassInHotbar();
            case COMPASS_INVENTORY -> isCompassInInventory();
            case ALWAYS -> true;
          };
    }
    return result;
  }

  private boolean isCompassHeld() {
    for (ItemStack stack : player.getHandSlots()) {
      if (DeadReckoning.isCompass(stack)) return true;
    }
    return false;
  }

  private boolean isCompassInHotbar() {
    return IntStream.range(0, Inventory.getSelectionSize())
            .anyMatch(i -> DeadReckoning.isCompass(player.getInventory().items.get(i)))
        || (isCompassHeld());
  }

  private boolean isCompassInInventory() {
    boolean result = true;
    if (!player.getInventory().hasAnyMatching(DeadReckoning::isCompass)) {
      if (trinkets == null || !trinkets.isEquipped(DeadReckoning::isCompass)) {
        result = isCompassHeld();
      }
    }
    return result;
  }

  private boolean shouldDrawBackground() {
    return DeadReckoning.CONFIG.style.background && shouldShowCompass();
  }

  private boolean shouldDrawMarkers() {
    return DeadReckoning.CONFIG.markers.enabled && shouldShowCompass();
  }

  private boolean shouldDrawDirections() {
    return DeadReckoning.CONFIG.style.directions != DeadReckoningConfig.Style.DirectionDisplay.NONE && shouldShowCompass();
  }

  private double yawToX(double yaw) {
    double ratio = (double) compassWidth / DeadReckoning.CONFIG.alignment.visibleArc;
    return yaw * ratio;
  }

  private int calcYOffset() {
    if (DeadReckoning.CONFIG.alignment.yOffset <= bossYOffset) {
      return DeadReckoning.CONFIG.alignment.yOffset + bossYOffset;
    }
    return DeadReckoning.CONFIG.alignment.yOffset;
  }

  private void renderBackground() {
    if (!shouldDrawBackground()) return;
    int bgColor = Objects.requireNonNullElse(Ints.tryParse(DeadReckoning.CONFIG.style.backgroundColor.substring(1), 16), WHITE);
    setColorWithOpacity(bgColor);
    int y =
        font.lineHeight - (DECORATION_HEIGHT / 2) + DeadReckoning.CONFIG.alignment.backgroundYOffset + calcYOffset();
    int endcap_width = 10;
    int width = compassEndX - compassStartX - (2 * endcap_width);
    ctx.blit(
        DECORATION_LEFT_TEXTURE,
        compassStartX,
        y,
        0f,
        0f,
        endcap_width,
        DECORATION_HEIGHT,
        endcap_width,
        DECORATION_HEIGHT);
    ctx.blit(
        DECORATION_TEXTURE,
        compassStartX + endcap_width,
        y,
        0f,
        0f,
        width,
        DECORATION_HEIGHT,
        width,
        DECORATION_HEIGHT);
    ctx.blit(
        DECORATION_RIGHT_TEXTURE,
        compassStartX + width + endcap_width,
        y,
        0f,
        0f,
        endcap_width,
        DECORATION_HEIGHT,
        endcap_width,
        DECORATION_HEIGHT);
    resetColor();
  }

  private void renderMarkers() {
    int z = 0;
    if (!shouldDrawMarkers()) return;
    List<AtlasMarker> sortedMarkers = getSortedMarkers(player);
    for (AtlasMarker marker : sortedMarkers) {
      if (marker.getDistance() <= 2) break;
      z += renderMarker(marker, z);
    }
  }

  private void drawTexture(ResourceLocation id, int width, int height) {
    int drawWidth = width + (width % 2);
    int drawHeight = height + (height % 2);
    ctx.blit(id, 0, 0, 0f, 0f, drawWidth, drawHeight, drawWidth, drawHeight);
  }

  private void drawMarker(AtlasMarker marker) {
    var texture = marker.getTexture();
    resetColor();
    drawTexture(texture.id(), texture.width(), texture.height());
    if (marker.hasAccent()) {
      setColor(marker.getColor(), 1);
      drawAccent(texture);
    }
  }

  private void drawAccent(AtlasMarker.Texture texture) {
    drawTexture(texture.accentId(), texture.width(), texture.height());
  }

  private int renderMarker(AtlasMarker marker, int z) {
    int zIncrease = 0;
    AtlasMarker.Texture texture = marker.getTexture();
    if (texture != null) {

      float markerX = (float) (centerX + yawToX(marker.getYaw()));
      float halfWidth = texture.width() / 2.0f;

      if ((markerX - halfWidth > compassStartX && markerX + halfWidth < compassEndX)) {

        float halfHeight = texture.height() / 2.0f;

        float scale = marker.calcScale();
        float yOffset = font.lineHeight + calcYOffset() + DeadReckoning.CONFIG.alignment.markerYOffset;
        resetColor();

        ctx.pose().pushPose();
        if (texture.id() == null && texture.stack() == null) {
          ctx.pose().translate(markerX, yOffset, z + ITEM_Z_OFFSET);
          ctx.pose().scale(scale, scale, 1);
          ctx.pose().translate((int) -halfWidth, (int) -halfHeight, 0);
          setColorWithOpacity(marker.getColor());
          drawBanner();
          zIncrease = 1;

        } else if (texture.stack() != null) {
          ctx.pose().translate(markerX, yOffset, z);
          ctx.pose().scale(scale, scale, 1);
          ctx.pose().translate((int) -halfWidth, (int) -halfHeight, 0);
          ctx.renderItem(texture.stack(), 0, 0);
          zIncrease = 8;

        } else {
          ctx.pose().translate(markerX, yOffset, z + ITEM_Z_OFFSET);
          ctx.pose().scale(scale, scale, 1);
          ctx.pose().translate((int) -halfWidth, (int) -halfHeight, 0);
          drawMarker(marker);
          zIncrease = 1;
        }
        ctx.pose().popPose();
        resetColor();
      }
    }
    return zIncrease;
  }

  private void drawBanner() {
    ctx.blit(
        ResourceLocation.tryParse("textures/map/decorations/white_banner.png"),
        0,
        0,
        16,
        16,
        0,
        0,
        8,
        8,
        8,
        8);
  }

  private void setColor(int color, float alpha) {
    RenderSystem.setShaderColor(FastColor.ARGB32.red(color) / 255.0F, FastColor.ARGB32.green(color) / 255.0F, FastColor.ARGB32.blue(color) / 255.0F, alpha);
  }

  private void setColorWithOpacity(int color) {
    setColor(color, DeadReckoning.CONFIG.style.opacity);
  }

  private void resetColor() {
    RenderSystem.setShaderColor(1, 1, 1, 1);
  }

  private void renderDirections() {
    if (!shouldDrawDirections()) return;
    int textColor = Objects.requireNonNullElse(Ints.tryParse(DeadReckoning.CONFIG.style.textColor.substring(1), 16), WHITE);
    setColorWithOpacity(textColor);
    int angle = 0;
    for (Direction direction : Direction.values()) {
      double yaw = Mth.wrapDegrees((double) angle - player.getYRot());
      angle += 45;
      if (DeadReckoning.CONFIG.style.directions == DeadReckoningConfig.Style.DirectionDisplay.CARDINAL && direction.ordinal() % 2 == 1) continue;
      double x = centerX + yawToX(yaw);
      double halfWidth = font.width(direction.abbrev) / 2.0;
      if (x - halfWidth > compassStartX && x + halfWidth < compassEndX) {
        x -= halfWidth;
        var text = Component.literal(direction.abbrev());

        ctx.drawString(
            font, text, (int) x, calcYOffset(), textColor, DeadReckoning.CONFIG.style.textShadow);
      }
    }
  }

  private List<AtlasMarker> getSortedMarkers(Player player) {
    WorldLandmarks landmarks = WorldSummary.of(player.level()).landmarks();
    if (landmarks == null) {
      return new ArrayList<>();
    }

    return landmarks.keySet(SurveyorClient.getExploration()).entries().stream()
        .map(entry -> landmarks.get(entry.getKey(), entry.getValue()))
        .filter(Objects::nonNull)
        .filter(e -> e.contains(LandmarkComponentTypes.POS))
        .map(landmark -> new AtlasMarker(player, landmark))
        .sorted(Comparator.comparingDouble(AtlasMarker::getDistance))
        .toList()
        .reversed();
  }

  public enum Direction {
    SOUTH("S"),
    SOUTHWEST("SW"),
    WEST("W"),
    NORTHWEST("NW"),
    NORTH("N"),
    NORTHEAST("NE"),
    EAST("E"),
    SOUTHEAST("SE");

    private final String abbrev;

    Direction(String abbrev) {
      this.abbrev = abbrev;
    }

    public String abbrev() {
      return abbrev;
    }
  }
}
