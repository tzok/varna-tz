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
import java.util.ArrayList; // Added for List
import java.util.List; // Added for List
import java.util.stream.Collectors;
// XML Processing Imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fr.orsay.lri.varna.models.VARNAConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
// VARNA Imports
import fr.orsay.lri.varna.exceptions.ExceptionExportFailed; // Added for ExceptionExportFailed
import fr.orsay.lri.varna.models.rna.ModeleBP;
import fr.orsay.lri.varna.models.rna.ModeleBPStyle;
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
          System.out.println("Initial SVG saved successfully.");

          // 7. Post-process SVG to remove discontinuous backbone lines and filter text labels
          try {
            postProcessSvg(outputFilename, structureData); // Renamed method call
            System.out.println("SVG post-processed successfully.");
          } catch (Exception xmlException) {
            System.err.println("Error during SVG post-processing:");
            xmlException.printStackTrace();
          }
        } catch (Exception e) {
          // Catch other potential exceptions during RNA processing/drawing
          System.err.println("An unexpected error occurred during RNA processing/drawing:");
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

  // Method to parse SVG, remove discontinuous backbone lines, and filter text labels
  private static void postProcessSvg(String svgFilePath, StructureData structureData) throws Exception {
    if (structureData == null || structureData.nucleotides == null || structureData.nucleotides.isEmpty()) {
      // No data to process
      System.err.println("Warning: No nucleotide data found for SVG post-processing.");
      return;
    }

    // 1. Find indices BEFORE which a discontinuity occurs
    List<Integer> discontinuityIndices = new ArrayList<>();
    for (int i = 0; i < structureData.nucleotides.size() - 1; i++) {
      Nucleotide current = structureData.nucleotides.get(i);
      Nucleotide next = structureData.nucleotides.get(i + 1);
      if (current != null && next != null) {
        // Check if numbering is NOT consecutive (current.number + 1 != next.number)
        if (current.number + 1 != next.number) {
          discontinuityIndices.add(i); // Add the index of the nucleotide BEFORE the break
          System.out.println(
              "Detected numbering discontinuity between index "
                  + i
                  + " (number "
                  + current.number
                  + ") and index "
                  + (i + 1)
                  + " (number "
                  + next.number
                  + ")");
        }
      }
    }

    if (discontinuityIndices.isEmpty()) {
      System.out.println("No numbering discontinuities found. SVG not modified.");
      System.out.println("No numbering discontinuities found.");
      // Continue processing for text labels even if no backbone lines are removed
    }

    // --- Determine External Nucleotides ---
    int n = structureData.nucleotides.size();
    boolean[] isExternal = new boolean[n];
    if (n > 0) {
      isExternal[0] = true; // First nucleotide of sequence is external
      isExternal[n - 1] = true; // Last nucleotide of sequence is external
      // Check for breaks *before* index i (meaning i is the start of a block)
      for (int i = 1; i < n; i++) {
        Nucleotide prev = structureData.nucleotides.get(i - 1);
        Nucleotide curr = structureData.nucleotides.get(i);
        if (prev != null && curr != null) {
          if (curr.number != prev.number + 1) {
            isExternal[i] = true; // i is the start of a new block, hence external
          }
        } else {
          isExternal[i] = true; // Treat null neighbor as a break
        }
      }
      // Check for breaks *after* index i (meaning i is the end of a block)
      for (int i = 0; i < n - 1; i++) {
        Nucleotide curr = structureData.nucleotides.get(i);
        Nucleotide next = structureData.nucleotides.get(i + 1);
        if (curr != null && next != null) {
          if (next.number != curr.number + 1) {
            isExternal[i] = true; // i is the end of a block, hence external
          }
        } else {
          isExternal[i] = true; // Treat null neighbor as a break
        }
      }
      // Log the calculated external status for debugging
      // System.out.println("isExternal array: " + Arrays.toString(isExternal));
    }

    // --- Parse the SVG file ---
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new File(svgFilePath));
    doc.getDocumentElement().normalize();

    // 3. Find all backbone lines
    NodeList lineNodes = doc.getElementsByTagName("line");
    List<Element> backboneLines = new ArrayList<>();
    for (int i = 0; i < lineNodes.getLength(); i++) {
      Node node = lineNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        // Check if the line has the specific stroke attribute for backbone lines
        if ("rgb(35%, 35%, 35%)".equals(element.getAttribute("stroke"))) {
          backboneLines.add(element);
        }
      }
    }

    // Safety check: Ensure number of backbone lines matches expectation (n-1)
    if (backboneLines.size() != structureData.nucleotides.size() - 1) {
      System.err.println(
          "Warning: Expected "
              + (structureData.nucleotides.size() - 1)
              + " backbone lines, but found "
              + backboneLines.size()
              + ". SVG modification might be incorrect.");
      // Decide whether to proceed or abort. For now, let's proceed with caution.
    }

    // 4. Identify and remove lines corresponding to discontinuities
    // Iterate in reverse order of indices to avoid index shifting issues during removal
    List<Element> linesToRemove = new ArrayList<>();
    System.out.println("--- Identifying Backbone Lines to Remove ---");
    System.out.println("Discontinuity indices (line before break): " + discontinuityIndices);
    System.out.println("Total backbone lines found: " + backboneLines.size());
    /* // Optional: Log all found backbone lines
    for(int k=0; k<backboneLines.size(); k++) {
        System.out.println("  Line " + k + ": " + backboneLines.get(k).toString());
    }
    */
    for (int i = discontinuityIndices.size() - 1; i >= 0; i--) {
      int indexToRemove = discontinuityIndices.get(i);
      if (indexToRemove >= 0 && indexToRemove < backboneLines.size()) {
        Element lineElement = backboneLines.get(indexToRemove);
        System.out.println(
            "  Marking for removal: Line index "
                + indexToRemove
                + ", Element: "
                + lineElement.getAttribute("x1")
                + ","
                + lineElement.getAttribute("y1")
                + " -> "
                + lineElement.getAttribute("x2")
                + ","
                + lineElement.getAttribute("y2")); // Log coordinates
        linesToRemove.add(lineElement);
      } else {
        System.err.println(
            "Warning: Calculated discontinuity index "
                + indexToRemove
                + " is out of bounds for the found backbone lines (size: "
                + backboneLines.size()
                + "). Skipping removal.");
      }
    }

    // Perform removal
    int removedCount = 0;
    for (Element line : linesToRemove) {
      Node parent = line.getParentNode();
      if (parent != null) {
        parent.removeChild(line);
        removedCount++;
      }
    }
    System.out.println("Removed " + removedCount + " backbone line(s) due to discontinuities.");

    System.out.println("Marked " + linesToRemove.size() + " backbone line(s) for removal.");

    // Perform removal
    int removedCount = 0;
    for (Element line : linesToRemove) {
      Node parent = line.getParentNode();
      if (parent != null) {
        parent.removeChild(line);
        removedCount++;
      }
    }
    System.out.println("Actually removed " + removedCount + " backbone line(s).");

    // --- Filter Text Labels ---
    System.out.println("--- Filtering Text Labels ---");
    NodeList textNodes = doc.getElementsByTagName("text");
    List<Element> textLabelsToRemove = new ArrayList<>();
    List<Element> numberLabels = new ArrayList<>(); // Store potential number labels in order

    // Find all text elements likely representing numbers
    for (int i = 0; i < textNodes.getLength(); i++) {
      Node node = textNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        // Check for the specific fill attribute
        if ("rgb(25%, 25%, 25%)".equals(element.getAttribute("fill"))) {
          // Basic check if content looks like a number
          if (element.getTextContent() != null && element.getTextContent().matches("\\d+")) {
            numberLabels.add(element);
          }
        }
      }
    }

    // Assume the order of these labels corresponds to the nucleotide sequence order
    if (numberLabels.size() != n) {
      System.err.println(
          "Warning: Found "
              + numberLabels.size()
              + " potential number labels, but expected "
              + n
              + ". Text label filtering might be incorrect.");
      // Decide how to proceed. For now, we'll attempt filtering anyway.
    }

    int labelsChecked = 0;
    for (int i = 0; i < numberLabels.size() && i < n; i++) { // Iterate up to the minimum size
      Element textElement = numberLabels.get(i);
      String textContent = textElement.getTextContent();
      try {
        int number = Integer.parseInt(textContent);
        labelsChecked++;
        boolean isExt = isExternal[i]; // Get calculated external status
        // Check conditions: keep if divisible by 10 OR external
        boolean keepLabel = (number % 10 == 0) || isExt;

        // Add detailed log
        System.out.println(
            "  Label Check: Index="
                + i
                + ", Number="
                + number
                + ", isExternal="
                + isExt
                + ", Keep="
                + keepLabel
                + ", TextContent='"
                + textContent
                + "'");

        if (!keepLabel) {
          textLabelsToRemove.add(textElement);
        }
      } catch (NumberFormatException e) {
        System.err.println(
            "Warning: Could not parse number from text element content: '"
                + textContent
                + "'. Skipping filtering for this element.");
      } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println(
            "Warning: Index mismatch ("
                + i
                + ") accessing isExternal array. Skipping filtering for this element.");
        // This might happen if numberLabels.size() != n
      }
    }

    // Remove the identified text labels
    int removedTextCount = 0;
    for (Element textElement : textLabelsToRemove) {
      Node parent = textElement.getParentNode();
      if (parent != null) {
        parent.removeChild(textElement);
        removedTextCount++;
      }
    }
    System.out.println(
        "Checked "
            + labelsChecked
            + " number labels. Removed "
            + removedTextCount
            + " text label(s) based on filtering rules.");

    // --- Write the modified DOM back to the SVG file ---
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(svgFilePath));
    transformer.transform(source, result);
  }
}
