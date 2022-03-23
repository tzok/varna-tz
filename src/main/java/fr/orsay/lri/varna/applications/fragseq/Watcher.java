package fr.orsay.lri.varna.applications.fragseq;

import java.util.ArrayList;

public class Watcher extends Thread {

  private FragSeqTreeModel _model;
  private boolean _terminated = false;

  public Watcher(FragSeqTreeModel model) {
    _model = model;
  }

  public void run() {
    while (!_terminated) {
      ArrayList<String> folders = _model.getFolders();
      for (String path : folders) {
        _model.addFolder(path);
        System.out.println("Watching [" + path + "]");
      }
      try {
        this.sleep(1000);
      } catch (InterruptedException e) {
      }
    }
  }

  public void finish() {
    _terminated = true;
  }
}
