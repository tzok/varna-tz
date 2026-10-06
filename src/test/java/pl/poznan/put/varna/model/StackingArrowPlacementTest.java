package pl.poznan.put.varna.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class StackingArrowPlacementTest {

  @Test
  void parsesCanonicalValues() {
    assertEquals(
        Optional.of(StackingArrowPlacement.CENTERED), StackingArrowPlacement.parse("centered"));
    assertEquals(
        Optional.of(StackingArrowPlacement.FIRST_PARTNER),
        StackingArrowPlacement.parse("first-partner"));
    assertEquals(
        Optional.of(StackingArrowPlacement.SECOND_PARTNER),
        StackingArrowPlacement.parse("second-partner"));
    assertEquals(
        Optional.of(StackingArrowPlacement.BOTH_PARTNERS),
        StackingArrowPlacement.parse("both-partners"));
    assertEquals(
        Optional.of(StackingArrowPlacement.OPPOSING_PARTNERS),
        StackingArrowPlacement.parse("opposing-partners"));
  }

  @Test
  void parsesAllDocumentedAliases() {
    assertEquals(StackingArrowPlacement.CENTERED, StackingArrowPlacement.parse("center").get());
    assertEquals(StackingArrowPlacement.CENTERED, StackingArrowPlacement.parse("middle").get());
    assertEquals(StackingArrowPlacement.CENTERED, StackingArrowPlacement.parse("midpoint").get());

    assertEquals(StackingArrowPlacement.FIRST_PARTNER, StackingArrowPlacement.parse("first").get());
    assertEquals(
        StackingArrowPlacement.FIRST_PARTNER, StackingArrowPlacement.parse("near-first").get());
    assertEquals(
        StackingArrowPlacement.FIRST_PARTNER,
        StackingArrowPlacement.parse("near-first-partner").get());

    assertEquals(
        StackingArrowPlacement.SECOND_PARTNER, StackingArrowPlacement.parse("second").get());
    assertEquals(
        StackingArrowPlacement.SECOND_PARTNER, StackingArrowPlacement.parse("near-second").get());
    assertEquals(
        StackingArrowPlacement.SECOND_PARTNER,
        StackingArrowPlacement.parse("near-second-partner").get());
    assertEquals(StackingArrowPlacement.SECOND_PARTNER, StackingArrowPlacement.parse("last").get());
    assertEquals(
        StackingArrowPlacement.SECOND_PARTNER, StackingArrowPlacement.parse("last-partner").get());

    assertEquals(StackingArrowPlacement.BOTH_PARTNERS, StackingArrowPlacement.parse("both").get());
    assertEquals(
        StackingArrowPlacement.BOTH_PARTNERS, StackingArrowPlacement.parse("both-ends").get());
    assertEquals(StackingArrowPlacement.BOTH_PARTNERS, StackingArrowPlacement.parse("ends").get());
    assertEquals(
        StackingArrowPlacement.BOTH_PARTNERS, StackingArrowPlacement.parse("double").get());

    assertEquals(
        StackingArrowPlacement.OPPOSING_PARTNERS, StackingArrowPlacement.parse("opposing").get());
    assertEquals(
        StackingArrowPlacement.OPPOSING_PARTNERS,
        StackingArrowPlacement.parse("bidirectional").get());
    assertEquals(
        StackingArrowPlacement.OPPOSING_PARTNERS, StackingArrowPlacement.parse("inverse").get());
    assertEquals(
        StackingArrowPlacement.OPPOSING_PARTNERS,
        StackingArrowPlacement.parse("inverse-both").get());
  }

  @Test
  void normalizesCaseUnderscoreAndSpace() {
    assertEquals(
        Optional.of(StackingArrowPlacement.FIRST_PARTNER),
        StackingArrowPlacement.parse("FIRST_PARTNER"));
    assertEquals(
        Optional.of(StackingArrowPlacement.OPPOSING_PARTNERS),
        StackingArrowPlacement.parse("Opposing Partners"));
    assertEquals(
        Optional.of(StackingArrowPlacement.BOTH_PARTNERS),
        StackingArrowPlacement.parse(" Both-Partners "));
  }

  @Test
  void invalidOrNullIsAbsent() {
    assertTrue(StackingArrowPlacement.parse("nonsense").isEmpty());
    assertTrue(StackingArrowPlacement.parse("").isEmpty());
    assertTrue(StackingArrowPlacement.parse("   ").isEmpty());
    assertTrue(StackingArrowPlacement.parse(null).isEmpty());
  }

  @Test
  void structureDataFallsBackToCenteredWhenAbsent() {
    StructureData sd = new StructureData();
    StackingArrowPlacementParseResult result = sd.parseStackingArrowPlacement();
    assertEquals(StackingArrowPlacement.CENTERED, result.getPlacement());
    assertTrue(result.usedDefault());
  }

  @Test
  void structureDataFallsBackToCenteredWhenInvalid() {
    StructureData sd = new StructureData();
    sd.stackingArrowPlacement = "garbage";
    StackingArrowPlacementParseResult result = sd.parseStackingArrowPlacement();
    assertEquals(StackingArrowPlacement.CENTERED, result.getPlacement());
    assertTrue(result.usedDefault());
  }

  @Test
  void structureDataParsesExplicitValue() {
    StructureData sd = new StructureData();
    sd.stackingArrowPlacement = "opposing-partners";
    StackingArrowPlacementParseResult result = sd.parseStackingArrowPlacement();
    assertEquals(StackingArrowPlacement.OPPOSING_PARTNERS, result.getPlacement());
    assertFalse(result.usedDefault());
  }
}
