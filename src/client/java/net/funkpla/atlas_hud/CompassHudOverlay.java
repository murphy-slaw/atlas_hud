package net.funkpla.atlas_hud;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.surveyor.landmark.Landmark;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CompassHudOverlay implements HudRenderCallback {

    private final AtlasHudConfig config = AutoConfig.getConfigHolder(AtlasHudConfig.class).getConfig();
    private int centerX;
    private int compassWidth;
    private int compassStartX;
    private int compassEndX;
    private int alpha;
    private float markerScale;
    private GuiGraphics ctx;
    private Font font;
    private Player player;

    @Override
    public void onHudRender(GuiGraphics ctx, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
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

        renderBackground();

        for (AtlasMarker marker : getSortedMarkers(level, player)) {
            if (marker.getDistance() <= 2)
                break;
            renderMarker(marker);
        }

        renderDirections();
    }

    private double yawToX(double yaw) {
        double ratio = (double) compassWidth / config.CompassArc;
        return yaw * ratio;
    }

    private void renderBackground() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int y = font.lineHeight + 1 + config.CompassOffset;
        ctx.fill(compassStartX, y, compassEndX, y + 1, config.CompassBackgroundColor + alpha);

        RenderSystem.disableBlend();
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
                float[] accent = marker.getColor().getTextureDiffuseColors();
                ctx.setColor(accent[0], accent[1], accent[2], config.CompassOpacity / 100f);
                drawAccent(marker.getTexture(), markerX);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            RenderSystem.defaultBlendFunc();
        }
    }

    private void renderDirections() {
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

                ctx.drawString(font, text, (int) x, config.CompassOffset + 1, config.CompassTextColor + alpha, true);
            }
            angle += 45;
        }
        RenderSystem.disableBlend();
    }

    private List<AtlasMarker> getSortedMarkers(ClientLevel level, Player player) {
        Map<Landmark<?>, MarkerTexture> landmarks = WorldAtlasData.getOrCreate(level).getEditableLandmarks();
        return landmarks.keySet().stream().map(landmark -> new AtlasMarker(player, landmark, landmarks.get(landmark))).sorted(Comparator.comparingDouble(AtlasMarker::getDistance).reversed()).toList();
    }

    public enum Direction {
        SOUTH("S"), SOUTHWEST("SW"), WEST("W"), NORTHWEST("NW"), NORTH("N"), NORTHEAST("NE"), EAST("E"), SOUTHEAST(
                "SE");

        private final String abbrev;

        Direction(String abbrev) {
            this.abbrev = abbrev;
        }

        public String abbrev() {
            return abbrev;
        }
    }
}

