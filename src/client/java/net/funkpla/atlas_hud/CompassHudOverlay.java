package net.funkpla.atlas_hud;

import static net.funkpla.atlas_hud.AtlasHudMod.MOD_ID;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.surveyor.client.SurveyorClient;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import java.util.*;
import java.util.stream.IntStream;
import lombok.Getter;
import me.shedaniel.math.Color;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CompassHudOverlay implements HudRenderCallback {
  private static final int BASE_BOSSBAR_OFFSET = 12;
  private static final int BOSSBAR_HEIGHT = 19;
  private static final ResourceLocation DECORATION_TEXTURE =
      new ResourceLocation(MOD_ID, "textures/gui/decoration.png");
  private static final ResourceLocation DECORATION_LEFT_TEXTURE =
      new ResourceLocation(MOD_ID, "textures/gui/left.png");
  private static final ResourceLocation DECORATION_RIGHT_TEXTURE =
      new ResourceLocation(MOD_ID, "textures/gui/right.png");
  private static final int DECORATION_HEIGHT = 5;
  private static final TagKey<Item> COMPASS_ITEMS =
      TagKey.create(
          BuiltInRegistries.ITEM.key(),
          new ResourceLocation(AtlasHudMod.MOD_ID, "shows_compass_ribbon"));
  private static final AtlasHudConfig config = AtlasHudMod.getConfig();
  @Getter private int centerX;
  @Getter private int compassWidth;
  @Getter private int compassStartX;
  @Getter private int compassEndX;
  private float alpha;
  private int bossYOffset;
  private float markerScale;
  private float minMarkerScale;
  private GuiGraphics ctx;
  private Font font;
  private Player player;

  @Override
  public void onHudRender(GuiGraphics ctx, float tickDelta) {
    Minecraft client = Minecraft.getInstance();
    this.ctx = ctx;
    int windowWidth = ctx.guiWidth();
    centerX = windowWidth / 2;
    compassWidth = (int) (windowWidth * (config.CompassWidth / 100d));
    compassStartX = centerX - (compassWidth / 2);
    compassEndX = centerX + (compassWidth / 2);
    alpha = config.CompassOpacity / 100f;
    markerScale = config.MarkerScale / 100f;
    minMarkerScale = config.MinMarkerScale / 100f;
    font = client.gui.getFont();
    player = client.player;
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
    return switch (config.DisplayRule) {
      case COMPASS_HELD -> isCompassHeld();
      case COMPASS_HOTBAR -> isCompassInHotbar();
      case COMPASS_INVENTORY -> isCompassInInventory();
      default -> true;
    };
      boolean result = false;
      if (AtlasHudClient.isHUD_ENABLED()) {
          result = switch (config.DisplayRule) {
              case COMPASS_HELD -> isCompassHeld();
              case COMPASS_HOTBAR -> isCompassInHotbar();
              case COMPASS_INVENTORY -> isCompassInInventory();
              case ALWAYS -> true;
          };
      }
      return result;
  }

  private boolean isCompassHeld() {
    for (ItemStack hand : player.getHandSlots()) {
      if (hand.is(COMPASS_ITEMS)) return true;
    }
    return false;
  }

  private boolean isCompassInHotbar() {
    return IntStream.range(0, Inventory.getSelectionSize())
            .anyMatch(i -> player.getInventory().items.get(i).is(COMPASS_ITEMS))
        || (isCompassHeld());
  }

  private boolean isCompassInInventory() {
    if (player.getInventory().contains(COMPASS_ITEMS)) return true;
    return isCompassHeld();
  }

  private boolean shouldDrawBackground() {
    return config.DrawBackground && shouldShowCompass();
  }

  private boolean shouldDrawMarkers() {
    return config.ShowMarkers && shouldShowCompass();
  }

  private boolean shouldDrawDirections() {
    return config.ShowDirections && shouldShowCompass();
  }

  private double yawToX(double yaw) {
    double ratio = (double) compassWidth / config.CompassArc;
    return yaw * ratio;
  }

  private int calcYOffset() {
    if (config.CompassOffset <= bossYOffset) {
      return config.CompassOffset + bossYOffset;
    }
    return config.CompassOffset;
  }

  private void renderBackground() {
    if (!shouldDrawBackground()) return;
    Color bgColor = Color.ofTransparent(config.CompassBackgroundColor);
    setColor(bgColor);
    int y =
        font.lineHeight - (DECORATION_HEIGHT / 2) + config.CompassBackgroundOffset + calcYOffset();
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
    RenderSystem.setShaderColor(1, 1, 1, 1);
  }

  private void renderMarkers() {
    if (!shouldDrawMarkers()) return;
    List<AtlasMarker> sortedMarkers = getSortedMarkers(player);
    for (int i = 0; i < sortedMarkers.size(); i++) {
      AtlasMarker marker = sortedMarkers.get(i);
      if (marker.getDistance() <= 2) break;
      renderMarker(marker, i);
    }
  }

  private float calcMarkerScale(AtlasMarker marker) {
    float chunkDistance = (float) ((marker.getDistance() / 64f) + 1f);
    return 1f / chunkDistance;
  }

  private float calcScale(AtlasMarker marker) {
    return Float.min(markerScale, Float.max(calcMarkerScale(marker), minMarkerScale));
  }

  private void drawTexture(ResourceLocation id, int width, int height) {
    int drawWidth = width + (width % 2);
    int drawHeight = height + (height % 2);
    int y = font.lineHeight - (drawHeight / 2) + calcYOffset() + 1;
    ctx.blit(id, 0, y, 0f, 0f, drawWidth, drawHeight, drawWidth, drawHeight);
  }

  private void drawMarker(AtlasMarker.Texture texture) {
    drawTexture(texture.id(), texture.width(), texture.height());
  }

  private void drawAccent(AtlasMarker.Texture texture) {
    drawTexture(texture.accentId(), texture.width(), texture.height());
  }

  private void renderMarker(AtlasMarker marker, int z) {
    AtlasMarker.Texture texture = marker.getTexture();
    if (texture == null) return;

    double markerX = centerX + yawToX(marker.getYaw());
    double halfWidth = texture.width() / 2.0d;
    int drawX = (int) (markerX - halfWidth);

    if (!(markerX - halfWidth > compassStartX && markerX + halfWidth < compassEndX)) return;

    float scale = calcScale(marker);
    float yOffset =
        font.lineHeight
            - (texture.height() / 2f * scale)
            + calcYOffset()
            + config.CompassMarkerOffset
            + 1;

    ctx.pose().pushPose();
    ctx.pose().translate(drawX, yOffset, z * 16);
    ctx.pose().scale(scale, scale, 1);

    if (texture.id() == null && texture.stack() == null) {
      Color color = Color.ofTransparent(marker.getColor());
      setColor(color);
      ctx.blit(
          new ResourceLocation("textures/map/map_icons.png"), 0, 0, 8, 8, 80, 0, 8, 8, 128, 128);
    } else if (texture.stack() != null) {
      RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
      ctx.renderItem(texture.stack(), 0, 0);

    } else {

      RenderSystem.setShaderColor(1, 1, 1, alpha);
      drawMarker(texture);
      if (marker.hasAccent()) {
        Color accent = Color.ofTransparent(marker.getColor());
        setColor(accent);
        drawAccent(marker.getTexture());
      }
    }
    ctx.pose().popPose();
    RenderSystem.setShaderColor(1, 1, 1, 1);
  }

  private void setColor(Color color) {
    RenderSystem.setShaderColor(
        color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
  }

  private void renderDirections() {
    if (!shouldDrawDirections()) return;
    Color textColor = Color.ofOpaque(config.CompassTextColor);
    RenderSystem.setShaderColor(1,1,1,alpha);
    int angle = 0;
    for (Direction direction : Direction.values()) {
      double yaw = Mth.wrapDegrees((double) angle - player.getYRot());
      double x = centerX + yawToX(yaw);
      double halfWidth = font.width(direction.abbrev) / 2.0;
      if (x - halfWidth > compassStartX && x + halfWidth < compassEndX) {
        x -= halfWidth;
        var text = Component.literal(direction.abbrev());

        ctx.drawString(font, text, (int) x, calcYOffset() + 1, textColor.getColor(), config.TextDropShadow);
      }
      angle += 45;
    }
  }

  private List<AtlasMarker> getSortedMarkers(Player player) {
    WorldLandmarks landmarks = AtlasHudClient.getLandmarks();
    if (landmarks == null) {
      return new ArrayList<>();
    }

    return landmarks.keySet(SurveyorClient.getExploration()).entries().stream()
        .map(entry -> landmarks.get(entry.getKey(), entry.getValue()))
        .filter(Objects::nonNull)
        .map(landmark -> new AtlasMarker(player, landmark))
        .sorted(Comparator.comparingDouble(AtlasMarker::getDistance).reversed())
        .toList();
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
