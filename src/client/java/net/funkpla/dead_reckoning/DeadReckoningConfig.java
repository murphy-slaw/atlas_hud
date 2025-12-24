package net.funkpla.dead_reckoning;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;

import java.util.List;

public class DeadReckoningConfig extends WrappedConfig {
  public enum DisplayWhen {
    ALWAYS, COMPASS_HELD, COMPASS_HOTBAR, COMPASS_INVENTORY
  }

  @Comment("When to display the compass HUD.")
  @Comment("Overridden if disabled via hotkey.")
  public DisplayWhen displayMode = DisplayWhen.COMPASS_HELD;

  // @Matches("([a-z0-9_.-]:)?[a-z0-9/._-]+") https://github.com/sisby-folk/mcqoy/issues/3
  @Comment("What items to consider compasses for non-always display modes.")
  @Comment("Overridden by #dead_reckoning:compasses (or #c:compasses) if present.")
  public List<String> compasses = ValueList.create("", "minecraft:compass", "minecraft:recovery_compass");

  public Style style = new Style();

  public static class Style implements Section {
    public enum DirectionDisplay {
      INTERCARDINAL,
      CARDINAL,
      NONE
    }

    @Comment("What level of compass directions to display as as abbreviated text.")
    public DirectionDisplay directions = DirectionDisplay.INTERCARDINAL;

    @Comment("Whether to render the line underneath the compass area.")
    public boolean background = true;

    @FloatRange(min = 0.1F, max = 1.0F)
    @Comment("How opaque to render all compass elements.")
    public float opacity = 1.0F;

    // @Matches("#[0-9A-Fa-f]{6}") https://github.com/sisby-folk/mcqoy/issues/3
    @Comment("What color to render the compass background decoration in.")
    @Comment("6-digit hex code, starting with #")
    public String backgroundColor = "#000000";

    // @Matches("#[0-9A-Fa-f]{6}") https://github.com/sisby-folk/mcqoy/issues/3
    @Comment("What color to render text (e.g. directions) in.")
    @Comment("6-digit hex code, starting with #")
    public String textColor = "#FFFFFF";

    @Comment("Whether to render text (e.g. directions) with a shadow.")
    public boolean textShadow = true;
  }

  public Markers markers = new Markers();

  public static class Markers implements Section {
    @Comment("Whether to show Surveyor markers on the compass at all.")
    public boolean enabled = true;

    @Comment("The maximum size to render markers on the compass.")
    @FloatRange(min = 0.1F, max = 1.0F)
    public float scale = 1.0F;

    @Comment("The minimum size to render markers on the compass.")
    @Comment("Used to represent the waypoint being further away.")
    @FloatRange(min = 0.1F, max = 1.0F)
    public float minScale = 0.1F;
  }

  public Alignment alignment = new Alignment();

  public static class Alignment implements Section {
    @Comment("The arc (in degrees) the compass should display at once.")
    @IntegerRange(min = 30, max = 360)
    public int visibleArc = 240;

    @Comment("The portion of the screen the compass should occupy.")
    @FloatRange(min = 0.1F, max = 1.0F)
    public float screenWidth = 0.9F;

    @DisplayName("Y Offset")
    @Comment("The number of pixels below the top of the screen to render the compass.")
    public int yOffset = 4;

    @DisplayName("Background Y Offset")
    @Comment("The number of pixels to lower the compass background decoration by.")
    public int backgroundYOffset = 0;

    @DisplayName("Marker Y Offset")
    @Comment("The number of pixels to lower markers by.")
    public int markerYOffset = 0;
  }
}
