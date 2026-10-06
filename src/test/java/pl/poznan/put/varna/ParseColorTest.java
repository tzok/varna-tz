package pl.poznan.put.varna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ParseColorTest {

  @Test
  void nullIsEmpty() {
    assertTrue(AdvancedDrawer.parseColor(null).isEmpty());
  }

  @Test
  void emptyIsEmpty() {
    assertTrue(AdvancedDrawer.parseColor("").isEmpty());
  }

  @Test
  void namedColors() {
    assertEquals(Optional.of(Color.RED), AdvancedDrawer.parseColor("red"));
    assertEquals(Optional.of(Color.RED), AdvancedDrawer.parseColor("RED"));
    assertEquals(Optional.of(Color.BLUE), AdvancedDrawer.parseColor("blue"));
    assertEquals(Optional.of(Color.GREEN), AdvancedDrawer.parseColor("green"));
  }

  @Test
  void hexSixDigits() {
    assertEquals(new Color(0x12, 0x34, 0x56), AdvancedDrawer.parseColor("#123456").get());
  }

  @Test
  void hexThreeDigitsDecodedAsPlainInteger() {
    // Color.decode("#123") parses the digits as a single hex integer 0x000123
    assertEquals(new Color(0x000123), AdvancedDrawer.parseColor("#123").get());
  }

  @Test
  void commaSeparatedRgb() {
    assertEquals(new Color(10, 20, 30), AdvancedDrawer.parseColor("10,20,30").get());
  }

  @Test
  void commaSeparatedRgbWithSpaces() {
    assertEquals(new Color(1, 2, 3), AdvancedDrawer.parseColor("1, 2, 3").get());
  }

  @Test
  void unknownNameIsEmpty() {
    assertTrue(AdvancedDrawer.parseColor("cerulean").isEmpty());
  }

  @Test
  void badRgbComponentsIsEmpty() {
    assertTrue(AdvancedDrawer.parseColor("1,2,x").isEmpty());
  }

  @Test
  void badHexIsEmpty() {
    assertTrue(AdvancedDrawer.parseColor("##12345").isEmpty());
  }
}
