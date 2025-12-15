package net.funkpla.atlas_hud;

import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class AtlasMarker {
    private final double distance;
    private final double pitch;
    private final double yaw;
    private final MarkerTexture texture;
    private final Integer color;

    public AtlasMarker(Player player, Landmark landmark, MarkerTexture texture) {
        BlockPos pos = landmark.get(LandmarkComponentTypes.POS);
        assert (pos != null);
        double dx = pos.getX() - player.getX();
        double dy = pos.getY() - player.getEyeHeight();
        double dz = pos.getZ() - player.getZ();
        distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        pitch = -Mth.atan2(dy, distance) * Mth.RAD_TO_DEG;
        yaw = Mth.wrapDegrees(-(Mth.atan2(dx, dz) * Mth.RAD_TO_DEG + (double) player.getYRot()));
        this.texture = texture;
        this.color = landmark.get(LandmarkComponentTypes.COLOR);
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

    public Integer getColor() {
        return color;
    }

    public ResourceLocation getAccentId() {
        return texture.accentId();
    }

    public int getHeight() {
        return texture.textureHeight();
    }

    public int getWidth() {
        return texture.textureWidth();
    }
}
