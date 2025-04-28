package pl.poznan.put.varna;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Color;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap; // Added for Map
import java.util.Map; // Added for Map
import java.util.Optional;
import java.util.stream.Collectors;
import fr.orsay.lri.varna.models.rna.ModeleBP;
import pl.poznan.put.structure.formats.BpSeq; // Import BpSeq
import pl.poznan.put.structure.formats.DotBracket; // Import DotBracket if needed later
import pl.poznan.put.structure.formats.ImmutableBpSeq; // Import ImmutableBpSeq
import pl.poznan.put.structure.formats.ImmutableDefaultDotBracket; // Import if needed later
import pl.poznan.put.varna.model.BasePair;
import pl.poznan.put.varna.model.Nucleotide;
import pl.poznan.put.varna.model.StructureData;

public class AdvancedDrawer {
  // Utility method to parse color strings
  public static Optional<Color> parseColor(String colorString) {
    if (colorString == null || colorString.isEmpty()) {
      return Optional.empty();
    }
    try {
      // Basic color name handling (add more as needed)
      if (colorString.equalsIgnoreCase("red")) return Optional.of(Color.RED);
      if (colorString.equalsIgnoreCase("blue")) return Optional.of(Color.BLUE);
      if (colorString.equalsIgnoreCase("green")) return Optional.of(Color.GREEN);
      // Add more standard color names...

      // Handle hex format like #RRGGBB or #RGB
      if (colorString.startsWith("#")) {
        return Optional.of(Color.decode(colorString));
      }
      // Handle comma-separated RGB like "R,G,B"
      String[] rgb = colorString.split(",");
      if (rgb.length == 3) {
        return Optional.of(
            new Color(
                Integer.parseInt(rgb[0].trim()),
                Integer.parseInt(rgb[1].trim()),
                Integer.parseInt(rgb[2].trim())));
      }
    } catch (NumberFormatException e) {
      System.err.println("Warning: Could not parse color string: " + colorString);
      return Optional.empty();
    }
    System.err.println("Warning: Unknown color format: " + colorString);
    return Optional.empty();
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: java pl.poznan.put.varna.AdvancedDrawer <path_to_json_file>");
      System.exit(1);
    }

    String jsonFilePath = args[0];
    File jsonFile = new File(jsonFilePath);

    if (!jsonFile.exists() || !jsonFile.isFile()) {
      System.err.println("Error: File not found or is not a valid file: " + jsonFilePath);
      System.exit(1);
    }

    ObjectMapper objectMapper = new ObjectMapper();
    StructureData structureData = null;

    try {
      structureData = objectMapper.readValue(jsonFile, StructureData.class);
      System.out.println("Successfully parsed and validated JSON file: " + jsonFilePath);

      // Parse colors after loading
      if (structureData.nucleotides != null) {
        for (Nucleotide n : structureData.nucleotides) {
          n.parsedColor = parseColor(n.color);
        }
      }
      if (structureData.basePairs != null) {
        for (BasePair bp : structureData.basePairs) {
          bp.parsedColor = parseColor(bp.color);
        }
      }

      System.out.println("Parsed data summary: " + structureData);

      // Create BpSeq object from canonical pairs
      try {
        BpSeq bpSeq = createBpSeqFromStructureData(structureData);
        System.out.println("Generated BpSeq object:");
        System.out.println(bpSeq); // Print the BpSeq object (or specific info)
        // Optionally, convert BpSeq to DotBracket if needed:
        // DotBracket dotBracket = ImmutableDefaultDotBracket.fromBpSeq(bpSeq);
        // System.out.println("Dot-Bracket (from BpSeq): " + dotBracket.structure());
      } catch (IllegalArgumentException e) {
        System.err.println("Error creating BpSeq structure: " + e.getMessage());
        // Optionally print stack trace for debugging
        // e.printStackTrace();
      }

    } catch (InvalidFormatException e) {
      // Handle errors specifically related to invalid enum values (validation failure)
      System.err.println("Error: Invalid value found in JSON file: " + jsonFilePath);
      System.err.println("Invalid value: '" + e.getValue() + "' for field: " + e.getPathReference());
      // Provide context about allowed values if it's one of our enums
      if (e.getTargetType().equals(ModeleBP.Stericity.class)) {
        System.err.println(
            "Allowed values for stericity are: "
                + Arrays.stream(ModeleBP.Stericity.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", ")));
      } else if (e.getTargetType().equals(ModeleBP.Edge.class)) {
        System.err.println(
            "Allowed values for edge types are: "
                + Arrays.stream(ModeleBP.Edge.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", ")));
      }
      e.printStackTrace();
      System.exit(1);
    } catch (IOException e) {
      // Handle other general IO/parsing errors
      System.err.println("Error reading or parsing JSON file: " + jsonFilePath);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static BpSeq createBpSeqFromStructureData(StructureData structureData)
      throws IllegalArgumentException {
    if (structureData == null
        || structureData.nucleotides == null
        || structureData.nucleotides.isEmpty()) {
      throw new IllegalArgumentException("Cannot create BpSeq: No nucleotides found in input data.");
    }

    int n = structureData.nucleotides.size();
    // Map nucleotide ID to its 0-based index
    Map<Integer, Integer> idToIndexMap = new HashMap<>();
    // Map nucleotide ID to the Nucleotide object itself
    Map<Integer, Nucleotide> idToNucleotideMap = new HashMap<>();

    for (int i = 0; i < n; i++) {
      Nucleotide nucleotide = structureData.nucleotides.get(i);
      if (nucleotide != null) {
        idToIndexMap.put(nucleotide.id, i);
        idToNucleotideMap.put(nucleotide.id, nucleotide);
      } else {
        // BpSeq requires contiguous sequence, handle nulls if necessary
        throw new IllegalArgumentException("Null nucleotide found at index " + i + ". Cannot create valid BpSeq.");
      }
    }

    // Map 0-based index to its pairing partner's 0-based index
    int[] pairMap = new int[n];
    Arrays.fill(pairMap, -1); // -1 indicates unpaired

    if (structureData.basePairs != null) {
      for (BasePair bp : structureData.basePairs) {
        // Check if the canonical field is present and true
        if (bp.canonical != null && bp.canonical) {
          Integer index1 = idToIndexMap.get(bp.id1);
          Integer index2 = idToIndexMap.get(bp.id2);

          if (index1 != null && index2 != null) {
            // Check for conflicts
            if (pairMap[index1] != -1 || pairMap[index2] != -1) {
              System.err.println(
                  "Warning: Conflicting canonical base pair involving nucleotides with IDs "
                      + bp.id1
                      + " and "
                      + bp.id2
                      + ". Skipping this pair for BpSeq generation.");
              continue;
            }
            pairMap[index1] = index2;
            pairMap[index2] = index1;
          } else {
            System.err.println(
                "Warning: Could not find index for nucleotide IDs "
                    + bp.id1
                    + " or "
                    + bp.id2
                    + " in canonical base pair. Skipping for BpSeq.");
          }
        }
      }
    }

    // Build the BpSeq string representation: "index character pair_index" (1-based indexing)
    StringBuilder bpSeqString = new StringBuilder();
    for (int i = 0; i < n; i++) {
      Nucleotide nucleotide = structureData.nucleotides.get(i);
      // BpSeq format uses 1-based indexing for sequence position and pair position
      int seqIndex = i + 1;
      // BpSeq uses 0 for unpaired, otherwise 1-based index of partner
      int pairIndex = (pairMap[i] == -1) ? 0 : pairMap[i] + 1;
      // Get the character, throw error if null/empty
      String character = nucleotide.character;
      if (character == null || character.isEmpty()) {
        throw new IllegalArgumentException(
            "Cannot create BpSeq: Nucleotide with ID "
                + nucleotide.id
                + " (at index "
                + i
                + ") is missing its character ('char' field).");
      }
      // Ensure single character for standard BpSeq (take first char if multi)
      char bpSeqChar = character.charAt(0);

      bpSeqString
          .append(seqIndex)
          .append(" ")
          .append(bpSeqChar)
          .append(" ")
          .append(pairIndex)
          .append("\n");
    }

    // Parse the string using BioCommons
    return ImmutableBpSeq.fromString(bpSeqString.toString());
  }
}
