package pl.poznan.put.varna;

import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionNAViewAlgorithm;
import fr.orsay.lri.varna.exceptions.ExceptionUnmatchedClosingParentheses;
import fr.orsay.lri.varna.exceptions.ExceptionWritingForbidden;
import fr.orsay.lri.varna.models.VARNAConfig;
import fr.orsay.lri.varna.models.rna.ModeleBP;
import fr.orsay.lri.varna.models.rna.RNA;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class ExtendedSecondaryStructureDrawer {
  private static final List<Character> OPENING =
      "([{<ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(i -> (char) i).collect(Collectors.toList());
  private static final List<Character> CLOSING =
      ")]}>abcdefghijklmnopqrstuvwxyz".chars().mapToObj(i -> (char) i).collect(Collectors.toList());

  private ExtendedSecondaryStructureDrawer() {
    super();
  }

  public static void main(final String[] args)
      throws IOException,
          ExceptionNAViewAlgorithm,
          ExceptionWritingForbidden,
          ExceptionFileFormatOrSyntax,
          ExceptionUnmatchedClosingParentheses {
    if (args.length != 1) {
      System.err.println("Usage: extended-drawer FILE");
      System.err.println("FILE contains multiple lines in the following format:");
      System.err.println();
      System.err.println("seq uAGGGUUAGGGUuAGGGUUAGGGU");
      System.err.println("cWH ..([{...)]}...([{...)]}.");
      System.err.println("cWH ........([{...)]}.......");
      System.err.println("cHW ..([{...............)]}.");
      return;
    }

    final File file = new File(args[0]);
    final File outputFile = File.createTempFile("varna-tz", ".svg");
    final List<String> lines = ExtendedSecondaryStructureDrawer.readLines(file);
    ExtendedSecondaryStructureDrawer.draw(lines, outputFile);
    System.out.println(outputFile);
  }

  private static List<String> readLines(final File file) throws IOException {
    try (final InputStream stream = new FileInputStream(file);
        final Reader reader = new InputStreamReader(stream, Charset.defaultCharset());
        final BufferedReader lineReader = new BufferedReader(reader)) {
      final List<String> lines = new ArrayList<>();
      String line;
      while ((line = lineReader.readLine()) != null) {
        lines.add(line);
      }
      return lines;
    }
  }

  public static void draw(final Collection<String> lines, final File outputFile)
      throws ExceptionNAViewAlgorithm,
          ExceptionWritingForbidden,
          ExceptionUnmatchedClosingParentheses,
          ExceptionFileFormatOrSyntax {
    final VARNAConfig config = new VARNAConfig();
    final RNA rna = new RNA(true);
    String sequence = "";
    String structure = "";

    for (final String line : lines) {
      final String[] tokens = line.split("\\s+");
      assert tokens.length == 2;

      if ("seq".equalsIgnoreCase(tokens[0])) {
        sequence = tokens[1];

        if (structure.isEmpty()) {
          rna.setRNA(sequence);
        } else {
          rna.setRNA(sequence, structure);
        }
      } else if ("cWW".equalsIgnoreCase(tokens[0])) {
        structure = tokens[1];
        rna.setRNA(sequence, structure);
      } else {
        ExtendedSecondaryStructureDrawer.addBasePairs(rna, tokens[0], tokens[1]);
      }
    }

    rna.drawRNANAView(config);
    rna.saveRNASVG(outputFile.getAbsolutePath(), config);
  }

  private static void addBasePairs(
      final RNA rna, final String leontisWesthof, final String dotBracket) {
    final char[] chars = leontisWesthof.toLowerCase().toCharArray();
    assert chars.length == 3;

    final ModeleBP.Stericity stericity = ExtendedSecondaryStructureDrawer.stericity(chars[0]);
    final ModeleBP.Edge edge5 = ExtendedSecondaryStructureDrawer.edge(chars[1]);
    final ModeleBP.Edge edge3 = ExtendedSecondaryStructureDrawer.edge(chars[2]);

    for (final int[] pair : ExtendedSecondaryStructureDrawer.pairsArray(dotBracket)) {
      rna.addBPAux(pair[0], pair[1], edge5, edge3, stericity);
    }
  }

  private static ModeleBP.Stericity stericity(final char c) {
    switch (c) {
      case 'c':
        return ModeleBP.Stericity.CIS;
      case 't':
        return ModeleBP.Stericity.TRANS;
      default:
        throw new IllegalArgumentException("Invalid stericity: " + c);
    }
  }

  private static ModeleBP.Edge edge(final char c) {
    switch (c) {
      case 'w':
        return ModeleBP.Edge.WC;
      case 'h':
        return ModeleBP.Edge.HOOGSTEEN;
      case 's':
        return ModeleBP.Edge.SUGAR;
      default:
        throw new IllegalArgumentException("Invalid edge: " + c);
    }
  }

  private static int[][] pairsArray(final String dotBracket) {
    final List<int[]> pairs = new ArrayList<>();
    final Map<Character, Stack<Integer>> stackMap = new HashMap<>();
    final char[] symbols = dotBracket.toCharArray();

    for (int i = 0; i < symbols.length; i++) {
      final char symbol = symbols[i];

      // a dot
      if (symbol == '.') {
        continue;
      }

      // an opening bracket
      final int openingOrder = ExtendedSecondaryStructureDrawer.OPENING.indexOf(symbol);

      if (openingOrder != -1) {
        stackMap.putIfAbsent(symbol, new Stack<>());
        stackMap.get(symbol).push(i);
        continue;
      }

      // a closing bracket
      final int closingOrder = ExtendedSecondaryStructureDrawer.CLOSING.indexOf(symbol);

      if (closingOrder != -1) {
        final int j =
            stackMap.get(ExtendedSecondaryStructureDrawer.OPENING.get(closingOrder)).pop();
        pairs.add(new int[] {i, j});
        continue;
      }

      throw new IllegalArgumentException("Invalid character: " + symbol);
    }

    // create an array out of that
    final int size = pairs.size();
    final int[][] pairsArray = new int[size][];

    for (int i = 0; i < size; i++) {
      final int[] pair = pairs.get(i);
      pairsArray[i] = pair;
    }

    return pairsArray;
  }
}
