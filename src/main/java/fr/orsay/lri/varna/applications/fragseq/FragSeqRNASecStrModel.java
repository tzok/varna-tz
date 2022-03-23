package fr.orsay.lri.varna.applications.fragseq;

import fr.orsay.lri.varna.models.rna.RNA;
import java.awt.datatransfer.DataFlavor;

public class FragSeqRNASecStrModel extends FragSeqModel {

  private RNA _r = null;

  public FragSeqRNASecStrModel(RNA r) {
    _r = r;
  }

  public String toString() {
    return _r.getName();
  }

  public String getID() {
    return _r.getID();
  }

  public RNA getRNA() {
    return _r;
  }

  public static DataFlavor Flavor =
      new DataFlavor(FragSeqRNASecStrModel.class, "RNA Sec Str Object");
}
