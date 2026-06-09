package pl.poznan.put.varna.model;

import fr.orsay.lri.varna.models.rna.ModeleBPStyle;
import java.util.Locale;
import java.util.Optional;

public enum StackingArrowPlacement {
  CENTERED("centered", ModeleBPStyle.BENT_STACKING_CENTERED),
  FIRST_PARTNER("first-partner", ModeleBPStyle.BENT_STACKING_FIRST_PARTNER),
  SECOND_PARTNER("second-partner", ModeleBPStyle.BENT_STACKING_SECOND_PARTNER),
  BOTH_PARTNERS("both-partners", ModeleBPStyle.BENT_STACKING_BOTH_PARTNERS);

  private final String jsonValue;
  private final double bentValue;

  StackingArrowPlacement(String jsonValue, double bentValue) {
    this.jsonValue = jsonValue;
    this.bentValue = bentValue;
  }

  public String getBentValueName() {
    return jsonValue;
  }

  public double getBentValue() {
    return bentValue;
  }

  public static Optional<StackingArrowPlacement> parse(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }

    String normalized = value.trim().toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
    switch (normalized) {
      case "center":
      case "centered":
      case "middle":
      case "midpoint":
        return Optional.of(CENTERED);
      case "first":
      case "first-partner":
      case "near-first":
      case "near-first-partner":
        return Optional.of(FIRST_PARTNER);
      case "second":
      case "second-partner":
      case "near-second":
      case "near-second-partner":
      case "last":
      case "last-partner":
        return Optional.of(SECOND_PARTNER);
      case "both":
      case "both-partners":
      case "both-ends":
      case "ends":
      case "double":
        return Optional.of(BOTH_PARTNERS);
      default:
        return Optional.empty();
    }
  }
}
