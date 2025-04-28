package pl.poznan.put.varna;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Color;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
// Import enums from ModeleBP
import fr.orsay.lri.varna.models.rna.ModeleBP;
// Import model classes
import pl.poznan.put.varna.model.BasePair;
import pl.poznan.put.varna.model.Nucleotide;
import pl.poznan.put.varna.model.StructureData;

public class AdvancedDrawer {

  // Nested classes Nucleotide, BasePair, and StructureData moved to separate files
  // in pl.poznan.put.varna.model package

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
      System.out.println("Parsed data summary: " + structureData);
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
}
