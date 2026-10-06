package pl.poznan.put.varna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;

class IntegrationSmokeTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "example.json",
        "example-icode.json",
        "pk-order.json",
        "rp01.json",
        "ss-example1.json",
        "ss-example2.json",
        "bug.json"
      })
  void drawsSvgForEveryFixture(String fixtureName) throws IOException {
    Path fixture = loadFixture(fixtureName);
    Path output = Files.createTempDirectory(fixtureName).resolve("output.svg");

    boolean success = AdvancedDrawer.run(fixture.toFile(), output.toString());

    assertTrue(success, "Pipeline should succeed for " + fixtureName);
    assertTrue(Files.exists(output), "SVG should be written for " + fixtureName);
    assertValidSvg(output, fixtureName);
  }

  private Path loadFixture(String name) throws IOException {
    String resource = "/fixtures/" + name;
    var url = getClass().getResource(resource);
    assertNotNull(url, "Missing test resource " + resource);
    return Path.of(url.getPath());
  }

  private void assertValidSvg(Path output, String fixtureName) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      Document doc = factory.newDocumentBuilder().parse(output.toFile());

      assertEquals("svg", doc.getDocumentElement().getTagName(), fixtureName);
      assertTrue(
          doc.getElementsByTagName("line").getLength() > 0,
          fixtureName + " should contain line elements");
      assertTrue(
          doc.getElementsByTagName("text").getLength() > 0,
          fixtureName + " should contain text elements");
      int nucleotides = nucleotideCount(fixtureName);
      assertTrue(
          doc.getElementsByTagName("circle").getLength() >= nucleotides,
          fixtureName + " should contain one circle per nucleotide");
    } catch (Exception e) {
      throw new AssertionError("Failed to parse SVG output for " + fixtureName, e);
    }
  }

  private int nucleotideCount(String fixtureName) throws IOException {
    File fixture = loadFixture(fixtureName).toFile();
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(fixture).get("nucleotides").size();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
