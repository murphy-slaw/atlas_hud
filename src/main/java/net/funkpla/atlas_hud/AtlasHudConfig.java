package net.funkpla.atlas_hud;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "atlas_hud")
public class AtlasHudConfig implements ConfigData {

    @ConfigEntry.Category("Display")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public final CompassDisplayRule DisplayRule = CompassDisplayRule.COMPASS_HELD;
    @ConfigEntry.Category("Display")
    public final boolean ShowDirections = true;
    @ConfigEntry.Category("Display")
    public final boolean ShowMarkers = true;
    @ConfigEntry.Category("Display")
    public final boolean DrawBackground = true;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 30, max = 360)
    public final int CompassArc = 240;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 100)
    public final int CompassWidth = 90;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public final int CompassOpacity = 100;
    @ConfigEntry.Category("Display")
    public final int CompassOffset = 4;
    @ConfigEntry.Category("Display")
    @ConfigEntry.BoundedDiscrete(min = 35, max = 100)
    public final int MarkerScale = 100;
    @ConfigEntry.Category("Display")
    @ConfigEntry.ColorPicker
    public final int CompassTextColor = 0xffffff;
    @ConfigEntry.Category("Display")
    @ConfigEntry.ColorPicker
    public final int CompassBackgroundColor = 0x000000;

    public enum CompassDisplayRule {
        ALWAYS, COMPASS_HELD, COMPASS_HOTBAR, COMPASS_INVENTORY
    }

}
