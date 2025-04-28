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

public class AdvancedDrawer {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Nucleotide {
    @JsonProperty("id")
    public int id;

    @JsonProperty("number")
    public int number;

    @JsonProperty("char")
    public String character; // Using String as 'char' might be multi-character like 'DA'

    @JsonProperty("color")
    public String color; // Store as String, can be parsed later if needed

    public Optional<Color> parseColor() {
      if (color == null || color.isEmpty()) {
        return Optional.empty();
      }
      try {
        // Basic color name handling (add more as needed)
        if (color.equalsIgnoreCase("red")) return Optional.of(Color.RED);
        if (color.equalsIgnoreCase("blue")) return Optional.of(Color.BLUE);
        if (color.equalsIgnoreCase("green")) return Optional.of(Color.GREEN);
        // Add more standard color names...

        // Handle hex format like #RRGGBB or #RGB
        if (color.startsWith("#")) {
          return Optional.of(Color.decode(color));
        }
        // Handle comma-separated RGB like "R,G,B"
        String[] rgb = color.split(",");
        if (rgb.length == 3) {
          return Optional.of(
              new Color(
                  Integer.parseInt(rgb[0].trim()),
                  Integer.parseInt(rgb[1].trim()),
                  Integer.parseInt(rgb[2].trim())));
        }
      } catch (NumberFormatException e) {
        System.err.println("Warning: Could not parse color string: " + color);
        return Optional.empty();
      }
      System.err.println("Warning: Unknown color format: " + color);
      return Optional.empty();
    }

    @Override
    public String toString() {
      return "Nucleotide{"
          + "id="
          + id
          + ", number="
          + number
          + ", character='"
          + character
          + '\''
          + ", color='"
          + color
          + '\''
          + '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BasePair {
    @JsonProperty("id1")
    public int id1; // References Nucleotide.id

    @JsonProperty("id2")
    public int id2; // References Nucleotide.id

    @JsonProperty("edge5")
    public EdgeType edge5;

    @JsonProperty("edge3")
    public EdgeType edge3;

    @JsonProperty("stericity")
    public Stericity stericity;

    @JsonProperty("canonical")
    public Boolean canonical; // Use Boolean object type to handle absence (null)

    @Override
    public String toString() {
      return "BasePair{"
          + "id1="
          + id1
          + ", id2="
          + id2
          + ", edge5="
          + edge5
          + ", edge3="
          + edge3
          + ", stericity="
          + stericity
          + ", canonical="
          + canonical
          + '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StructureData {
    @JsonProperty("nucleotides")
    public List<Nucleotide> nucleotides;

    @JsonProperty("basePairs")
    public List<BasePair> basePairs;

    @Override
    public String toString() {
      return "StructureData{"
          + "nucleotides="
          + (nucleotides != null ? nucleotides.size() : 0)
          + " items, basePairs="
          + (basePairs != null ? basePairs.size() : 0)
          + " items}";
    }
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
      if (e.getTargetType().equals(Stericity.class)) {
        System.err.println(
            "Allowed values for stericity are: "
                + Arrays.stream(Stericity.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", ")));
      } else if (e.getTargetType().equals(EdgeType.class)) {
        System.err.println(
            "Allowed values for edge types are: "
                + Arrays.stream(EdgeType.values())
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
