package pl.poznan.put.varna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.Color;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Nucleotide {
  @JsonProperty("id")
  public int id;

  @JsonProperty("number")
  public int number;

  @JsonProperty("char")
  public String character;

  @JsonProperty("color")
  public String color;

  // Transient field to store the parsed color object
  public transient Optional<Color> parsedColor = Optional.empty();

  // Getter for the parsed color
  public Optional<Color> getParsedColor() {
    return parsedColor;
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
