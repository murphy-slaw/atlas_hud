package net.funkpla.atlas_hud;

import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.surveyor.landmark.Landmark;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class AtlasMarker {
    private final double distance;
    private final double pitch;
    private final double yaw;
    private final MarkerTexture texture;
    private final DyeColor color;

    public AtlasMarker(Player player, Landmark<?> landmark, MarkerTexture texture) {
        double dx = landmark.pos().getX() - player.getX();
        double dy = landmark.pos().getY() - player.getEyeHeight();
        double dz = landmark.pos().getZ() - player.getZ();
        distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        pitch = -Mth.atan2(dy, distance) * Mth.RAD_TO_DEG;
        yaw = Mth.wrapDegrees(-(Mth.atan2(dx, dz) * Mth.RAD_TO_DEG + (double) player.getYRot()));
        this.texture = texture;
        this.color = landmark.color();
    }

    public double getDistance() {
        return distance;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public MarkerTexture getTexture() {
        return texture;
    }

    public boolean hasAccent() {
        return getColor() != null && getTexture().accentId() != null;
    }

    public DyeColor getColor() {
        return color;
    }

    public ResourceLocation getAccentId() {
        return texture.accentId();
    }

    public int getHeight() {
        return getTexture().textureHeight();
    }

    public int getWidth() {
        return getTexture().textureWidth();
    }
}
