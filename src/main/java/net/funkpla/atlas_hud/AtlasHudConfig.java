package net.funkpla.atlas_hud;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.math.Color;

@Config(name="atlas_hud")
public class AtlasHudConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min=30, max=360)
    public int CompassArc = 240;

    @ConfigEntry.BoundedDiscrete(min=50, max=100)
    public int CompassWidth = 90;

    @ConfigEntry.BoundedDiscrete(min=1, max=100)
    public int CompassOpacity = 100;

    public int CompassOffset = 4;

    @ConfigEntry.BoundedDiscrete(min=1, max=100)
    public int MarkerScale = 100;

    @ConfigEntry.ColorPicker
    public int CompassTextColor = Color.ofRGB(1,1,1).getColor();

    @ConfigEntry.ColorPicker
    public int CompassBackgroundColor = Color.ofRGB(0,0,0).getColor();


}
