package pl.poznan.put.varna.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NucleotideTest {

  private Nucleotide nucleotideWithNumber(Object number) {
    Nucleotide n = new Nucleotide();
    n.setNumber(number);
    return n;
  }

  @Test
  void numberAsJsonNumber() {
    Nucleotide n = nucleotideWithNumber(190);
    assertEquals("190", n.getNumber());
    assertEquals("190", n.getNumberLabel());
  }

  @Test
  void numberWithInsertionCode() {
    Nucleotide n = nucleotideWithNumber("190A");
    assertEquals("190A", n.getNumber());
    assertEquals("190A", n.getNumberLabel());
  }

  @Test
  void nullNumberYieldsEmptyLabel() {
    Nucleotide n = new Nucleotide();
    assertEquals("", n.getNumberLabel());
  }

  @Test
  void prefixOfNumericNumber() {
    assertEquals(java.util.Optional.of(190), nucleotideWithNumber(190).getNumberPrefix());
  }

  @Test
  void prefixOfInsertionCode() {
    assertEquals(java.util.Optional.of(190), nucleotideWithNumber("190A").getNumberPrefix());
  }

  @Test
  void prefixOfInsertionCodeWithSuffixDigits() {
    assertEquals(java.util.Optional.of(10), nucleotideWithNumber("10A1").getNumberPrefix());
  }

  @Test
  void prefixOfNegativeNumber() {
    assertEquals(java.util.Optional.of(-5), nucleotideWithNumber("-5B").getNumberPrefix());
  }

  @Test
  void prefixOfNonNumericIsAbsent() {
    assertTrue(nucleotideWithNumber("insA").getNumberPrefix().isEmpty());
  }

  @Test
  void prefixOfBlancoIsAbsent() {
    assertTrue(nucleotideWithNumber("").getNumberPrefix().isEmpty());
  }

  @Test
  void prefixOfWhitespaceTrimmed() {
    assertEquals(java.util.Optional.of(7), nucleotideWithNumber(" 7 ").getNumberPrefix());
  }
}
