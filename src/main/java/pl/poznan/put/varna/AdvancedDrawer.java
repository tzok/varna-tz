package pl.poznan.put.varna;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Color;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.orsay.lri.varna.models.VARNAConfig;
import fr.orsay.lri.varna.models.rna.ModeleBP;
import fr.orsay.lri.varna.models.rna.ModeleBPStyle; // Import ModeleBPStyle
import fr.orsay.lri.varna.models.rna.ModeleBase;
import fr.orsay.lri.varna.models.rna.ModelBaseStyle; // Import ModelBaseStyle
import fr.orsay.lri.varna.models.rna.RNA;
import pl.poznan.put.structure.formats.*;
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
          n.parsedOutlineColor = parseColor(n.outlineColor);
          n.parsedInnerColor = parseColor(n.innerColor);
          n.parsedNumberColor = parseColor(n.numberColor);
          n.parsedNameColor = parseColor(n.nameColor);
        }
      }
      if (structureData.basePairs != null) {
        for (BasePair bp : structureData.basePairs) {
          bp.parsedColor = parseColor(bp.color);
        }
      }

      System.out.println("Parsed data summary: " + structureData);

      // Create BpSeq object and DotBracket from canonical pairs
      BpSeq bpSeq = null;
      DotBracket dotBracket = null;
      try {
        bpSeq = createBpSeqFromStructureData(structureData);
        Converter converter = ImmutableDefaultConverter.of();
        dotBracket = converter.convert(bpSeq);
        System.out.println("Generated BpSeq and DotBracket structure from canonical pairs.");
        // System.out.println("Dot-Bracket:\n" + dotBracket); // Optional: print dot bracket
      } catch (IllegalArgumentException e) {
        System.err.println("Error creating base secondary structure: " + e.getMessage());
        System.exit(1); // Exit if we can't form the base structure
      }

      // Proceed with VARNA drawing if bpSeq and dotBracket were created
      if (bpSeq != null && dotBracket != null) {
        try {
          // 1. Get sequence string
          String sequence = bpSeq.sequence();
          // 2. Get dot-bracket structure string
          String structure = dotBracket.structure();

          // 3. Create RNA object (using canonical structure initially)
          RNA rna = new RNA(true); // true enables auxiliary base pairs
          rna.setRNA(sequence, structure);

          // 4. Add non-canonical pairs and apply colors
          Map<Integer, Integer> idToIndexMap = createIdToIndexMap(structureData);
          applyCustomizations(rna, structureData, idToIndexMap); // Renamed method call

          // 5. Configure and Draw
          VARNAConfig config = new VARNAConfig();
          config._numPeriod = 1;
          // Add any custom config settings here, e.g., config.setColorScheme(...)

          System.out.println("Calculating RNA layout...");
          rna.drawRNANAView(config); // Use NAView layout algorithm

          // 6. Save SVG
          String outputFilename = "output.svg"; // Default output filename
          System.out.println("Saving RNA visualization to: " + outputFilename);
          rna.saveRNASVG(outputFilename, config);
          System.out.println("SVG saved successfully.");
        } catch (Exception e) {
          // Catch other potential exceptions during RNA processing/drawing
          System.err.println("An unexpected error occurred during RNA drawing:");
          e.printStackTrace();
        }
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
      System.exit(1);
    } catch (IOException e) {
      // Handle other general IO/parsing errors
      System.err.println("Error reading or parsing JSON file: " + jsonFilePath);
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

  // Helper method to create the ID to 0-based index map
  private static Map<Integer, Integer> createIdToIndexMap(StructureData structureData) {
    Map<Integer, Integer> idToIndexMap = new HashMap<>();
    if (structureData != null && structureData.nucleotides != null) {
      for (int i = 0; i < structureData.nucleotides.size(); i++) {
        Nucleotide nucleotide = structureData.nucleotides.get(i);
        if (nucleotide != null) {
          idToIndexMap.put(nucleotide.id, i);
        }
      }
    }
    return idToIndexMap;
  }

  // Method to add non-canonical base pairs and apply colors/thickness to nucleotides and base pairs
  private static void applyCustomizations(
      RNA rna, StructureData structureData, Map<Integer, Integer> idToIndexMap) {

    // --- Apply Base Pair Customizations (Non-canonical, Color, Thickness) ---

    // 1. Build a lookup map for BasePair data based on nucleotide indices
    Map<String, BasePair> bpDataMap = new HashMap<>();
    if (structureData.basePairs != null) {
      for (BasePair bpData : structureData.basePairs) {
        Integer index1 = idToIndexMap.get(bpData.id1);
        Integer index2 = idToIndexMap.get(bpData.id2);
        if (index1 != null && index2 != null) {
          // Create a consistent key (e.g., "minIdx-maxIdx")
          String key = Math.min(index1, index2) + "-" + Math.max(index1, index2);
          bpDataMap.put(key, bpData);
        } else {
          System.err.println(
              "Warning: Skipping creation of lookup key for base pair involving missing nucleotide IDs: "
                  + bpData.id1
                  + ", "
                  + bpData.id2);
        }
      }
    }

    // 2. Add non-canonical pairs first (important for getAllBPs to include them)
    if (structureData.basePairs != null) {
      for (BasePair bpData : structureData.basePairs) {
        // Add non-canonical pairs using addBPAux
        if (bpData.canonical == null || !bpData.canonical) {
          Integer index1 = idToIndexMap.get(bpData.id1);
          Integer index2 = idToIndexMap.get(bpData.id2);
          if (index1 != null && index2 != null) {
            try {
              rna.addBPAux(index1, index2, bpData.edge5, bpData.edge3, bpData.stericity);
            } catch (Exception e) {
              System.err.println(
                  "Warning: Failed to add non-canonical base pair between indices "
                      + (index1 + 1)
                      + " and "
                      + (index2 + 1)
                      + ": "
                      + e.getMessage());
            }
          }
        }
      }
    }

    // 3. Iterate through VARNA's ModeleBP objects and apply styles
    for (ModeleBP modeleBP : rna.getAllBPs()) {
      int index1 = modeleBP.getIndex5();
      int index2 = modeleBP.getIndex3();

      // Create the lookup key
      String key = Math.min(index1, index2) + "-" + Math.max(index1, index2);
      BasePair bpData = bpDataMap.get(key);

      if (bpData != null) {
        // Get or create the style object
        ModeleBPStyle style = modeleBP.getStyle();
        if (style == null) {
          style = new ModeleBPStyle();
          modeleBP.setStyle(style);
        }

        // Apply color if present
        bpData.getParsedColor().ifPresent(style::setCustomColor);

        // Apply thickness if present
        if (bpData.thickness != null) {
          try {
            style.setThickness(bpData.thickness);
          } catch (NumberFormatException e) {
            // This shouldn't happen if thickness is Double, but good practice
            System.err.println(
                "Warning: Invalid thickness format for pair " + key + ": " + bpData.thickness);
          }
        }
      } else {
        // This might happen for canonical pairs added by setRNA if not present in JSON basePairs list
        // Or if there was an issue creating the key earlier.
        // System.err.println("Debug: No matching BasePair data found for ModeleBP key: " + key);
      }
    }

    // --- Apply Nucleotide Colors ---
    // Apply nucleotide colors
    if (structureData.nucleotides != null) {
      for (Nucleotide nucData : structureData.nucleotides) {
        Integer index = idToIndexMap.get(nucData.id);
        if (index != null) {
          try {
            // VARNA uses 0-based indexing for getBaseAt()
            ModeleBase modeleBase = rna.getBaseAt(index);
            if (modeleBase != null) {
              ModelBaseStyle style = modeleBase.getStyleBase();
              // Set the base number from JSON data
              modeleBase.setBaseNumber(nucData.number);
              // Apply colors if they were successfully parsed
              nucData.getParsedOutlineColor().ifPresent(style::setBaseOutlineColor);
              nucData.getParsedInnerColor().ifPresent(style::setBaseInnerColor);
              nucData.getParsedNumberColor().ifPresent(style::setBaseNumberColor);
              nucData.getParsedNameColor().ifPresent(style::setBaseNameColor);
            } else {
              System.err.println(
                  "Warning: Could not find ModeleBase object for nucleotide index "
                      + index
                      + " to apply color.");
            }
          } catch (IndexOutOfBoundsException e) {
            System.err.println(
                "Warning: Index out of bounds when trying to get ModeleBase at index " + index);
          }
        } else {
          System.err.println(
              "Warning: Could not find index for nucleotide ID " + nucData.id + " to apply color.");
        }
      }
    }
  }
}
