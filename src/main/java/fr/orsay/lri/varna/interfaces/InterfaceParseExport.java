package fr.orsay.lri.varna.interfaces;

import fr.orsay.lri.varna.VARNAPanel;

public interface InterfaceParseExport {
  void parse(String s, VARNAPanel vp);

  String export();
}
