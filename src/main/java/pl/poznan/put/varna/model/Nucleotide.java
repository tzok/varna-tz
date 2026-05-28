package pl.poznan.put.varna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.Color;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Nucleotide {
  private static final Pattern NUMBER_PREFIX_PATTERN = Pattern.compile("^([+-]?\\d+)");

  @JsonProperty("id")
  public int id;

  private String number;

  @JsonProperty("number")
  public void setNumber(Object number) {
    this.number = number == null ? null : String.valueOf(number);
  }

  @JsonProperty("number")
  public String getNumber() {
    return number;
  }

  public String getNumberLabel() {
    return number == null ? "" : number;
  }

  public Optional<Integer> getNumberPrefix() {
    if (number == null) {
      return Optional.empty();
    }

    Matcher matcher = NUMBER_PREFIX_PATTERN.matcher(number.trim());
    if (!matcher.find()) {
      return Optional.empty();
    }

    try {
      return Optional.of(Integer.parseInt(matcher.group(1)));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  @JsonProperty("char")
  public String character;

  @JsonProperty("outlineColor")
  public String outlineColor;

  @JsonProperty("innerColor")
  public String innerColor;

  @JsonProperty("nameColor")
  public String nameColor;

  // Transient fields to store the parsed color objects
  public transient Optional<Color> parsedOutlineColor = Optional.empty();
  public transient Optional<Color> parsedInnerColor = Optional.empty();
  public transient Optional<Color> parsedNameColor = Optional.empty();

  // Getters for the parsed colors (optional, but good practice)
  public Optional<Color> getParsedOutlineColor() {
    return parsedOutlineColor;
  }

  public Optional<Color> getParsedInnerColor() {
    return parsedInnerColor;
  }

  public Optional<Color> getParsedNameColor() {
    return parsedNameColor;
  }

  @Override
  public String toString() {
    return "Nucleotide{"
        + "id="
        + id
        + ", number='"
        + number
        + '\''
        + ", character='"
        + character
        + '\''
        + ", outlineColor='"
        + outlineColor
        + '\''
        + ", innerColor='"
        + innerColor
        + '\''
        + ", nameColor='"
        + nameColor
        + '\''
        + '}';
  }
}
