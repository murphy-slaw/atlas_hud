package net.funkpla.atlas_hud;

import static net.funkpla.atlas_hud.AtlasHudMod.MOD_ID;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.surveyor.landmark.Landmark;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.math.Color;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
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
      ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/decoration.png");
  private static final ResourceLocation DECORATION_LEFT_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/left.png");
  private static final ResourceLocation DECORATION_RIGHT_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/right.png");
  private static final int DECORATION_HEIGHT = 5;

    private static final TagKey<Item> COMPASS_ITEMS =
            TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    ResourceLocation.fromNamespaceAndPath(
                            AtlasHudMod.MOD_ID, "shows_compass_ribbon"));
    private final AtlasHudConfig config =
            AutoConfig.getConfigHolder(AtlasHudConfig.class).getConfig();
    private int centerX;
    private int compassWidth;
    private int compassStartX;
    private int compassEndX;
    private int alpha;
    private int bossYOffset;
    private float markerScale;
    private float minMarkerScale;
    private GuiGraphics ctx;
    private Font font;
    private ClientLevel level;
    private Player player;

  @Override
  public void onHudRender(GuiGraphics ctx, DeltaTracker deltaTracker) {
    Minecraft client = Minecraft.getInstance();
    this.ctx = ctx;
    int windowWidth = ctx.guiWidth();
    centerX = windowWidth / 2;
    compassWidth = (int) (windowWidth * (config.CompassWidth / 100d));
    compassStartX = centerX - (compassWidth / 2);
    compassEndX = centerX + (compassWidth / 2);
    alpha = config.CompassOpacity * 255 / 100 << 24;
    markerScale = config.MarkerScale / 100f;
    minMarkerScale = config.MinMarkerScale / 100f;
    font = client.gui.getFont();
    player = client.player;
    level = client.level;
    BossHealthOverlay bossOverlay = client.gui.getBossOverlay();

    bossYOffset = 0;
    if (!bossOverlay.events.isEmpty()) {
      bossYOffset = BASE_BOSSBAR_OFFSET + 2;
      for (int i = 1; i < bossOverlay.events.size(); i++) {
        bossYOffset += BOSSBAR_HEIGHT;
      }
    }

    renderBackground();
    renderMarkers();
    renderDirections();
  }

  private boolean shouldShowCompass() {
    return switch (config.DisplayRule) {
      case COMPASS_HELD -> isCompassHeld();
      case COMPASS_HOTBAR -> isCompassInHotbar();
      case COMPASS_INVENTORY -> isCompassInInventory();
      default -> true;
    };
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
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    Color bgColor = Color.ofOpaque(config.CompassBackgroundColor);
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
    RenderSystem.disableBlend();
  }

  private void renderMarkers() {
    if (!shouldDrawMarkers()) return;
    for (AtlasMarker marker : getSortedMarkers(level, player)) {
      if (marker.getDistance() <= 2) break;
      renderMarker(marker);
    }
  }

  private float calcMarkerScale(AtlasMarker marker) {
    float chunkDistance = (float) ((marker.getDistance() / 64f) + 1f);
    return 1f / chunkDistance;
  }

  private void drawTexture(ResourceLocation id, double x, int width, int height, float scale) {
    int baseWidth = (int) (height * scale);
    int drawWidth = baseWidth + (baseWidth % 2);
    int baseHeight = (int) (height * scale);
    int drawHeight = baseHeight + (baseHeight % 2);
    int y = font.lineHeight - (drawHeight / 2) + calcYOffset() + 1;
    ctx.blit(id, (int) x, y, 0f, 0f, drawWidth, drawHeight, drawWidth, drawHeight);
  }

  private void drawMarker(MarkerTexture texture, double x, float scale) {
    drawTexture(texture.id(), x, texture.textureWidth(), texture.textureHeight(), scale);
  }

  private void drawAccent(MarkerTexture texture, double x, float scale) {
    drawTexture(texture.accentId(), x, texture.textureWidth(), texture.textureHeight(), scale);
  }

  private void renderMarker(AtlasMarker marker) {
    double markerX = centerX + yawToX(marker.getYaw());
    double halfWidth = marker.getWidth() / 2.0d;
    if (markerX - halfWidth > compassStartX && markerX + halfWidth < compassEndX) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      markerX -= halfWidth;
      ctx.setColor(1, 1, 1, config.CompassOpacity / 100f);
      float scale = Float.min(markerScale, Float.max(calcMarkerScale(marker), minMarkerScale));
      drawMarker(marker.getTexture(), markerX, scale);
      if (marker.hasAccent()) {
        Color accent = Color.ofTransparent(marker.getColor());
        setColor(accent);
        drawAccent(marker.getTexture(), markerX, scale);
        RenderSystem.setShaderColor(1, 1, 1, 1);
      }
      RenderSystem.defaultBlendFunc();
    }
  }

  private void setColor(Color color) {
    ctx.setColor(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        config.CompassOpacity / 100f);
  }

  private void renderDirections() {
    if (!shouldDrawDirections()) return;
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    int angle = 0;
    for (Direction direction : Direction.values()) {
      double yaw = Mth.wrapDegrees((double) angle - player.getYRot());
      double x = centerX + yawToX(yaw);
      double halfWidth = font.width(direction.abbrev) / 2.0;
      if (x - halfWidth > compassStartX && x + halfWidth < compassEndX) {
        x -= halfWidth;
        var text = Component.literal(direction.abbrev());

        ctx.drawString(
            font,
            text,
            (int) x,
            calcYOffset() + 1,
            config.CompassTextColor + alpha,
            config.TextDropShadow);
      }
      angle += 45;
    }
    RenderSystem.disableBlend();
  }

  private List<AtlasMarker> getSortedMarkers(ClientLevel level, Player player) {
    Map<Landmark, MarkerTexture> landmarks =
        WorldAtlasData.getOrCreate(level).getEditableLandmarks(level);
    return landmarks.keySet().stream()
        .map(landmark -> new AtlasMarker(player, landmark, landmarks.get(landmark)))
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
