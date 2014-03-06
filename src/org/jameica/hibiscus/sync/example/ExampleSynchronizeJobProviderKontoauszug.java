package org.jameica.hibiscus.sync.example;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Job-Providers fuer den Abruf von Kontoauszuegen.
 */
@Lifecycle(Type.CONTEXT)
public class ExampleSynchronizeJobProviderKontoauszug implements ExampleSynchronizeJobProvider
{
  @Resource
  private ExampleSynchronizeBackend backend = null;

  // Liste der von diesem Backend implementierten Jobs
  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(ExampleSynchronizeJobKontoauszug.class);
  }};

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public List<SynchronizeJob> getSynchronizeJobs(Konto k)
  {
    Class<SynchronizeJobKontoauszug> type = SynchronizeJobKontoauszug.class;
    
    List<SynchronizeJob> jobs = new LinkedList<SynchronizeJob>();
    for (Konto kt:backend.getSynchronizeKonten(k))
    {
      try
      {
        if (!backend.supports(type,k)) // Checken, ob das ein passendes Konto ist
          continue;

        final SynchronizeOptions options = new SynchronizeOptions(kt);

        if (!options.getSyncKontoauszuege()) // Sync-Option zum Kontoauszugs-Abruf aktiv?
          continue;
        
        SynchronizeJobKontoauszug job = backend.create(type,kt); // erzeugt eine Instanz von ExampleSynchronizeJobKontoauszug
        job.setContext(SynchronizeJob.CTX_ENTITY,kt);
        jobs.add(job);
      }
      catch (Exception e)
      {
        Logger.error("unable to load synchronize jobs",e);
      }
    }

    return jobs;
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider#getJobTypes()
   */
  @Override
  public List<Class<? extends SynchronizeJob>> getJobTypes()
  {
    return JOBS;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Object o)
  {
    // Umsaetze und Salden werden zum Schluss ausgefuehrt,
    // damit die oben gesendeten Ueberweisungen gleich mit
    // erscheinen, insofern die Bank das unterstuetzt.
    return 1;
  }

}


