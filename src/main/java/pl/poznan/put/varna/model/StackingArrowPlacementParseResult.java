package pl.poznan.put.varna.model;

public class StackingArrowPlacementParseResult {
  private final StackingArrowPlacement placement;
  private final boolean usedDefault;

  private StackingArrowPlacementParseResult(StackingArrowPlacement placement, boolean usedDefault) {
    this.placement = placement;
    this.usedDefault = usedDefault;
  }

  public static StackingArrowPlacementParseResult explicit(StackingArrowPlacement placement) {
    return new StackingArrowPlacementParseResult(placement, false);
  }

  public static StackingArrowPlacementParseResult fallback(StackingArrowPlacement placement) {
    return new StackingArrowPlacementParseResult(placement, true);
  }

  public StackingArrowPlacement getPlacement() {
    return placement;
  }

  public boolean usedDefault() {
    return usedDefault;
  }
}
