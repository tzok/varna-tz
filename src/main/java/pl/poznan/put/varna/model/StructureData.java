package pl.poznan.put.varna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StructureData {
  @JsonProperty("nucleotides")
  public List<Nucleotide> nucleotides;

  @JsonProperty("basePairs")
  public List<BasePair> basePairs;

  @JsonProperty("stackings")
  public List<Stacking> stackings;

  @JsonProperty("drawingAlgorithm")
  public String drawingAlgorithm;

  @JsonProperty("stackingArrowPlacement")
  public String stackingArrowPlacement;

  @JsonProperty("stackingArrowGap")
  public Double stackingArrowGap;

  @JsonProperty("bpStyle")
  public String bpStyle;

  /**
   * Zero-based indices of nucleotides after which the backbone is discontinuous (i.e. the last
   * nucleotide of each strand except the final one). When present, these are honored in addition to
   * the numbering-discontinuity heuristic during SVG post-processing.
   */
  @JsonProperty("strandBreaks")
  public List<Integer> strandBreaks;

  public StackingArrowPlacementParseResult parseStackingArrowPlacement() {
    if (stackingArrowPlacement == null || stackingArrowPlacement.isBlank()) {
      return StackingArrowPlacementParseResult.fallback(StackingArrowPlacement.CENTERED);
    }

    return StackingArrowPlacement.parse(stackingArrowPlacement)
        .map(StackingArrowPlacementParseResult::explicit)
        .orElseGet(
            () -> StackingArrowPlacementParseResult.fallback(StackingArrowPlacement.CENTERED));
  }

  @Override
  public String toString() {
    return "StructureData{"
        + "nucleotides="
        + (nucleotides != null ? nucleotides.size() : 0)
        + " items, basePairs="
        + (basePairs != null ? basePairs.size() : 0)
        + " items, drawingAlgorithm='"
        + drawingAlgorithm
        + '\''
        + ", stackingArrowPlacement='"
        + stackingArrowPlacement
        + '\''
        + ", stackingArrowGap="
        + stackingArrowGap
        + ", bpStyle='"
        + bpStyle
        + '\''
        + ", strandBreaks="
        + (strandBreaks != null ? strandBreaks : "[]")
        + ", stackings="
        + (stackings != null ? stackings.size() : 0)
        + " items"
        + "}";
  }
}
