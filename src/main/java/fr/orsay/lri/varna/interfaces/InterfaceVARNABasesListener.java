package fr.orsay.lri.varna.interfaces;

import fr.orsay.lri.varna.models.rna.ModeleBase;
import java.awt.event.MouseEvent;

public interface InterfaceVARNABasesListener {

  /**
   * Reacts to click over base
   *
   * @param mb The base which has just been clicked
   */
  public void onBaseClicked(ModeleBase mb, MouseEvent e);
}
