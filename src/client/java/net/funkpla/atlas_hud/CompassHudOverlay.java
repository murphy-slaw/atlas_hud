package net.funkpla.atlas_hud;

import com.mojang.blaze3d.systems.RenderSystem;

import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.surveyor.landmark.Landmark;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.math.Color;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class CompassHudOverlay implements HudRenderCallback {

    private static final TagKey<Item> COMPASS_ITEMS =
            TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    new ResourceLocation(AtlasHudMod.MOD_ID, "shows_compass_ribbon"));
    private final AtlasHudConfig config =
            AutoConfig.getConfigHolder(AtlasHudConfig.class).getConfig();
    private int centerX;
    private int compassWidth;
    private int compassStartX;
    private int compassEndX;
    private int alpha;
    private float markerScale;
    private GuiGraphics ctx;
    private Font font;
    private ClientLevel level;
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
        alpha = config.CompassOpacity * 255 / 100 << 24;
        markerScale = config.MarkerScale / 100f;
        font = client.gui.getFont();
        player = client.player;
        level = client.level;

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

    private void renderBackground() {
        if (!shouldDrawBackground()) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int y = font.lineHeight + 1 + config.CompassOffset;
        ctx.fill(compassStartX, y, compassEndX, y + 1, config.CompassBackgroundColor + alpha);

        RenderSystem.disableBlend();
    }

    private void renderMarkers() {
        if (!shouldDrawMarkers()) return;
        for (AtlasMarker marker : getSortedMarkers(level, player)) {
            if (marker.getDistance() <= 2) break;
            renderMarker(marker);
        }
    }

    private void drawTexture(ResourceLocation id, double x, int width, int height) {
        int dw = (int) (width * markerScale);
        int dh = (int) (height * markerScale);
        int y = font.lineHeight - (dh / 2) + config.CompassOffset + 1;
        ctx.blit(id, (int) x, y, 0f, 0f, dw, dh, dw, dh);
    }

    private void drawMarker(MarkerTexture texture, double x) {
        drawTexture(texture.id(), x, texture.textureWidth(), texture.textureHeight());
    }

    private void drawAccent(MarkerTexture texture, double x) {
        drawTexture(texture.accentId(), x, texture.textureWidth(), texture.textureHeight());
    }

    private void renderMarker(AtlasMarker marker) {
        double markerX = centerX + yawToX(marker.getYaw());
        double halfWidth = marker.getWidth() / 2.0d;
        if (markerX - halfWidth > compassStartX && markerX + halfWidth < compassEndX) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            markerX -= halfWidth;
            ctx.setColor(1, 1, 1, config.CompassOpacity / 100f);
            drawMarker(marker.getTexture(), markerX);
            if (marker.hasAccent()) {
                assert marker.getColor() != null;
                float[] accent = colorToRGBA(Color.ofOpaque(marker.getColor()));
                ctx.setColor(accent[0], accent[1], accent[2], config.CompassOpacity / 100f);
                drawAccent(marker.getTexture(), markerX);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            RenderSystem.defaultBlendFunc();
        }
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
                        config.CompassOffset + 1,
                        config.CompassTextColor + alpha,
                        true);
            }
            angle += 45;
        }
        RenderSystem.disableBlend();
    }
    private float[] colorToRGBA(Color color) {
        return new float[] {
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        };
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
