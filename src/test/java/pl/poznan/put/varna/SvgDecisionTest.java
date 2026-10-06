package pl.poznan.put.varna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pl.poznan.put.varna.model.Nucleotide;
import pl.poznan.put.varna.model.StructureData;

class SvgDecisionTest {

  private Nucleotide nucleotide(int id, int number, String character) {
    return nucleotide(id, String.valueOf(number), character);
  }

  private Nucleotide nucleotide(int id, String number, String character) {
    Nucleotide n = new Nucleotide();
    n.id = id;
    n.setNumber(number);
    n.character = character;
    return n;
  }

  @Test
  void continuousNumberingHasNoBreaks() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 1, "A"),
            nucleotide(2, 2, "C"),
            nucleotide(3, 3, "G"),
            nucleotide(4, 4, "U"));

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    assertTrue(plan.discontinuityIndices.isEmpty());
    assertEquals(Set.of(0, 3), plan.labelsToKeep);
  }

  @Test
  void numberingJumpCreatesBreak() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 1, "A"),
            nucleotide(2, 2, "C"),
            nucleotide(3, 9, "G"),
            nucleotide(4, 10, "U"));

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    assertEquals(Set.of(1), plan.discontinuityIndices);
    // 1 and 2 kept from the break, 3 is the start of the break pair... only i and i+1 (2,3)
    assertEquals(Set.of(0, 1, 2, 3), plan.labelsToKeep);
  }

  @Test
  void everyTenthPrefixKept() {
    StructureData sd = new StructureData();
    sd.nucleotides = new java.util.ArrayList<>();
    for (int i = 1; i <= 25; i++) {
      sd.nucleotides.add(nucleotide(i, i, i % 4 == 0 ? "U" : "A"));
    }

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    // zero-based indices of numbers 10 and 20, plus first and last
    assertEquals(Set.of(0, 9, 19, 24), plan.labelsToKeep);
    assertTrue(plan.discontinuityIndices.isEmpty());
  }

  @Test
  void sharedTenthPrefixKeptOnlyOnce() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 9, "A"),
            nucleotide(2, "10", "C"),
            nucleotide(3, "10A", "G"),
            nucleotide(4, "10B", "U"),
            nucleotide(5, 11, "A"));

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    // Only the first residue with prefix 10 keeps its label; first and last always kept
    assertEquals(Set.of(0, 1, 4), plan.labelsToKeep);
  }

  @Test
  void insertionCodeWithinBlockDoesNotCreateBreak() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, "10", "A"),
            nucleotide(2, "10A", "C"),
            nucleotide(3, "11", "G"),
            nucleotide(4, 12, "U"));

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    // 10 -> 10A -> 11 -> 12 all step by +1 or 0, no discontinuities
    assertTrue(plan.discontinuityIndices.isEmpty());
    // 10 kept (tenth label), 10A dropped because prefix 10 was already kept,
    // 11 and 12 are not divisible by 10; first (0) and last (3) always kept
    assertEquals(Set.of(0, 3), plan.labelsToKeep);
  }

  @Test
  void explicitStrandBreaksHonored() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 1, "A"),
            nucleotide(2, 2, "C"),
            nucleotide(3, 3, "G"),
            nucleotide(4, 4, "U"));
    sd.strandBreaks = List.of(1);

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    assertEquals(Set.of(1), plan.discontinuityIndices);
    assertEquals(Set.of(0, 1, 2, 3), plan.labelsToKeep);
  }

  @Test
  void explicitStrandBreaksMergedWithHeuristic() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 1, "A"),
            nucleotide(2, 2, "C"),
            nucleotide(3, 5, "G"),
            nucleotide(4, 6, "U"),
            nucleotide(5, 7, "A"));
    sd.strandBreaks = List.of(2);

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    // heuristic break after index 1 (2 -> 5), explicit break after index 2
    assertEquals(Set.of(1, 2), plan.discontinuityIndices);
    assertEquals(Set.of(0, 1, 2, 3, 4), plan.labelsToKeep);
  }

  @Test
  void explicitStrandBreaksDeDuplicated() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 1, "A"),
            nucleotide(2, 2, "C"),
            nucleotide(3, 5, "G"),
            nucleotide(4, 6, "U"));
    sd.strandBreaks = List.of(1, 1);

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    // heuristic break after index 1 (2 -> 5) merged with the duplicate explicit break
    assertEquals(Set.of(1), plan.discontinuityIndices);
  }

  @Test
  void outOfRangeStrandBreaksIgnored() {
    StructureData sd = new StructureData();
    sd.nucleotides = List.of(nucleotide(1, 1, "A"), nucleotide(2, 2, "C"), nucleotide(3, 3, "G"));
    sd.strandBreaks = java.util.Arrays.asList(3, -1, null);

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    assertTrue(plan.discontinuityIndices.isEmpty());
    assertEquals(Set.of(0, 2), plan.labelsToKeep);
  }

  @Test
  void missingNumbersDisableHeuristic() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, "", "A"),
            nucleotide(2, "", "C"),
            nucleotide(3, (String) null, "G"),
            nucleotide(4, 4, "U"));

    AdvancedDrawer.SvgPlan plan = AdvancedDrawer.computeSvgPlan(sd);
    assertTrue(plan.discontinuityIndices.isEmpty());
  }
}
