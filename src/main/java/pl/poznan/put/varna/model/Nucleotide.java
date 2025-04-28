package pl.poznan.put.varna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Nucleotide {
  @JsonProperty("id")
  public int id;

  @JsonProperty("number")
  public int number;

  @JsonProperty("char")
  public String character; // Using String as 'char' might be multi-character like 'DA'

  @JsonProperty("color")
  public String color; // Store as String, can be parsed later if needed

  // parseColor method moved to AdvancedDrawer class

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
