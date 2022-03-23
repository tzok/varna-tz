package pl.poznan.put.varna;

import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionNAViewAlgorithm;
import fr.orsay.lri.varna.exceptions.ExceptionUnmatchedClosingParentheses;
import fr.orsay.lri.varna.exceptions.ExceptionWritingForbidden;
import fr.orsay.lri.varna.models.VARNAConfig;
import fr.orsay.lri.varna.models.rna.ModeleBP;
import fr.orsay.lri.varna.models.rna.RNA;
import java.io.File;
import java.io.IOException;

public final class TesterApp {
  private TesterApp() {
    super();
  }

  public static void main(final String[] args)
      throws ExceptionUnmatchedClosingParentheses, ExceptionFileFormatOrSyntax, IOException,
          ExceptionWritingForbidden, ExceptionNAViewAlgorithm {

    final VARNAConfig config = new VARNAConfig();

    final RNA rna = new RNA(true);
    rna.setRNA(
        "UCCCAUAACCACUAACUGCCGCGUCAUCCCUCAACUCCUUUCCCCUUACGAACCCUAGUCUCUCGA",
        "..................................................................");
    rna.addBPAux(10, 55, ModeleBP.Edge.WC, ModeleBP.Edge.WC, ModeleBP.Stericity.CIS);
    rna.addBPAux(11, 54, ModeleBP.Edge.WC, ModeleBP.Edge.WC, ModeleBP.Stericity.TRANS);
    rna.addBPAux(12, 53, ModeleBP.Edge.WC, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Stericity.CIS);
    rna.addBPAux(13, 52, ModeleBP.Edge.WC, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Stericity.TRANS);
    rna.addBPAux(14, 51, ModeleBP.Edge.WC, ModeleBP.Edge.SUGAR, ModeleBP.Stericity.CIS);
    rna.addBPAux(15, 50, ModeleBP.Edge.WC, ModeleBP.Edge.SUGAR, ModeleBP.Stericity.TRANS);
    rna.addBPAux(16, 49, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Edge.WC, ModeleBP.Stericity.CIS);
    rna.addBPAux(17, 48, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Edge.WC, ModeleBP.Stericity.TRANS);
    rna.addBPAux(18, 47, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Stericity.CIS);
    rna.addBPAux(
        19, 46, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Stericity.TRANS);
    rna.addBPAux(20, 45, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Edge.SUGAR, ModeleBP.Stericity.CIS);
    rna.addBPAux(21, 44, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Edge.SUGAR, ModeleBP.Stericity.TRANS);
    rna.addBPAux(22, 43, ModeleBP.Edge.SUGAR, ModeleBP.Edge.WC, ModeleBP.Stericity.CIS);
    rna.addBPAux(23, 42, ModeleBP.Edge.SUGAR, ModeleBP.Edge.WC, ModeleBP.Stericity.TRANS);
    rna.addBPAux(24, 41, ModeleBP.Edge.SUGAR, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Stericity.CIS);
    rna.addBPAux(25, 40, ModeleBP.Edge.SUGAR, ModeleBP.Edge.HOOGSTEEN, ModeleBP.Stericity.TRANS);
    rna.addBPAux(26, 39, ModeleBP.Edge.SUGAR, ModeleBP.Edge.SUGAR, ModeleBP.Stericity.CIS);
    rna.addBPAux(27, 38, ModeleBP.Edge.SUGAR, ModeleBP.Edge.SUGAR, ModeleBP.Stericity.TRANS);
    rna.drawRNANAView(config);

    final File tempFile = File.createTempFile("varna-tz", ".svg");
    rna.saveRNASVG(tempFile.getAbsolutePath(), config);

    System.out.println(tempFile);
  }
}
