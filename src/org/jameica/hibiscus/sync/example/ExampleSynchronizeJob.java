package org.jameica.hibiscus.sync.example;

import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;

/**
 * Marker-Interface fuer die vom Plugin unterstuetzten Jobs.
 */
public interface ExampleSynchronizeJob extends SynchronizeJob
{
  // Hier koennen wir jetzt eigene Funktionen definieren, die dann von
  // der JobGroup im ExampleSynchronizeBackend ausgefuehrt werden.
  
  /**
   * Fuehrt den Job aus.
   * @throws Exception
   */
  public void execute() throws Exception;

}


