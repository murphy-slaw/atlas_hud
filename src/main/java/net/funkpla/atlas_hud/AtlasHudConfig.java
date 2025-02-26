package net.funkpla.atlas_hud;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.math.Color;

@Config(name = "atlas_hud")
public class AtlasHudConfig implements ConfigData {

    @ConfigEntry.Category("Display")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public CompassDisplayRule DisplayRule = CompassDisplayRule.COMPASS_HELD;
    @ConfigEntry.Category("Display")
    public boolean ShowDirections = true;
    @ConfigEntry.Category("Display")
    public boolean ShowMarkers = true;
    @ConfigEntry.Category("Display")
    public boolean DrawBackground = true;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 30, max = 360)
    public int CompassArc = 240;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 100)
    public int CompassWidth = 90;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int CompassOpacity = 100;
    @ConfigEntry.Category("Display")
    public int CompassOffset = 4;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 35, max = 100)
    public int MarkerScale = 100;
    @ConfigEntry.Category("Display")
    @ConfigEntry.ColorPicker
    public int CompassTextColor = Color.ofRGB(1, 1, 1).getColor();
    @ConfigEntry.Category("Display")
    @ConfigEntry.ColorPicker
    public int CompassBackgroundColor = Color.ofRGB(0, 0, 0).getColor();

    public enum CompassDisplayRule {
        ALWAYS, COMPASS_HELD, COMPASS_HOTBAR, COMPASS_INVENTORY
    }

}
