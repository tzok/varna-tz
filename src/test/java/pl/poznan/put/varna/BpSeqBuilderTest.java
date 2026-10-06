package pl.poznan.put.varna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.orsay.lri.varna.models.rna.ModeleBP;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.poznan.put.varna.model.BasePair;
import pl.poznan.put.varna.model.Nucleotide;
import pl.poznan.put.varna.model.StructureData;

class BpSeqBuilderTest {

  private Nucleotide nucleotide(int id, int number, String character) {
    Nucleotide n = new Nucleotide();
    n.id = id;
    n.setNumber(number);
    n.character = character;
    return n;
  }

  private BasePair basePair(int id1, int id2, boolean canonical) {
    BasePair bp = new BasePair();
    bp.id1 = id1;
    bp.id2 = id2;
    bp.edge5 = ModeleBP.Edge.WC;
    bp.edge3 = ModeleBP.Edge.WC;
    bp.stericity = ModeleBP.Stericity.CIS;
    bp.canonical = canonical;
    return bp;
  }

  @Test
  void simpleCanonicalHairpin() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 1, "A"),
            nucleotide(2, 2, "C"),
            nucleotide(3, 3, "G"),
            nucleotide(4, 4, "U"));
    sd.basePairs = List.of(basePair(1, 4, true), basePair(2, 3, true), basePair(1, 3, false));

    var bpSeq = AdvancedDrawer.createBpSeqFromStructureData(sd);
    // Only canonical pairs contribute: 1 pairs 4, 2 pairs 3; non-canonical 1-3 ignored
    String expected = "1 A 4\n2 C 3\n3 G 2\n4 U 1\n";
    assertEquals(expected, bpSeq.toString());
  }

  @Test
  void nonCanonicalPairsIgnored() {
    StructureData sd = new StructureData();
    sd.nucleotides = List.of(nucleotide(1, 1, "A"), nucleotide(2, 2, "C"), nucleotide(3, 3, "G"));
    sd.basePairs = List.of(basePair(1, 3, false));

    var bpSeq = AdvancedDrawer.createBpSeqFromStructureData(sd);
    assertEquals("1 A 0\n2 C 0\n3 G 0\n", bpSeq.toString());
  }

  @Test
  void canonicalFlagAbsentMeansIgnored() {
    StructureData sd = new StructureData();
    sd.nucleotides = List.of(nucleotide(1, 1, "A"), nucleotide(2, 2, "C"), nucleotide(3, 3, "G"));
    BasePair bp = basePair(1, 3, false);
    bp.canonical = null;
    sd.basePairs = List.of(bp);

    var bpSeq = AdvancedDrawer.createBpSeqFromStructureData(sd);
    assertEquals("1 A 0\n2 C 0\n3 G 0\n", bpSeq.toString());
  }

  @Test
  void conflictingCanonicalPairIsSkipped() {
    StructureData sd = new StructureData();
    sd.nucleotides =
        List.of(
            nucleotide(1, 1, "A"),
            nucleotide(2, 2, "C"),
            nucleotide(3, 3, "G"),
            nucleotide(4, 4, "U"));
    sd.basePairs = List.of(basePair(1, 4, true), basePair(3, 4, true));

    var bpSeq = AdvancedDrawer.createBpSeqFromStructureData(sd);
    // Second pair conflicts on nucleotide 4 and must be skipped
    assertEquals("1 A 4\n2 C 0\n3 G 0\n4 U 1\n", bpSeq.toString());
  }

  @Test
  void unknownNucleotideIdsWarnAndSkip() {
    StructureData sd = new StructureData();
    sd.nucleotides = List.of(nucleotide(1, 1, "A"), nucleotide(2, 2, "C"), nucleotide(3, 3, "G"));
    sd.basePairs = List.of(basePair(1, 99, true));

    var bpSeq = AdvancedDrawer.createBpSeqFromStructureData(sd);
    assertEquals("1 A 0\n2 C 0\n3 G 0\n", bpSeq.toString());
  }

  @Test
  void missingCharacterThrows() {
    StructureData sd = new StructureData();
    sd.nucleotides = List.of(nucleotide(1, 1, "A"), nucleotide(2, 2, ""));
    assertThrows(
        IllegalArgumentException.class, () -> AdvancedDrawer.createBpSeqFromStructureData(sd));
  }

  @Test
  void nullNucleotidesThrows() {
    StructureData sd = new StructureData();
    assertThrows(
        IllegalArgumentException.class, () -> AdvancedDrawer.createBpSeqFromStructureData(sd));
  }

  @Test
  void emptyNucleotideListThrows() {
    StructureData sd = new StructureData();
    sd.nucleotides = List.of();
    assertThrows(
        IllegalArgumentException.class, () -> AdvancedDrawer.createBpSeqFromStructureData(sd));
  }
}
