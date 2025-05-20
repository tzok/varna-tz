package pl.poznan.put.varna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Stacking {
  @JsonProperty("id1")
  public int id1;

  @JsonProperty("id2")
  public int id2;

  @JsonProperty("color")
  public String color;

  @JsonProperty("thickness")
  public Double thickness;

  @Override
  public String toString() {
    return "Stacking{"
        + "id1="
        + id1
        + ", id2="
        + id2
        + ", color='"
        + color
        + '\''
        + ", thickness="
        + thickness
        + '}';
  }
}
