package org.jameica.hibiscus.sync.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Kontoauszugsabruf fuer eine Beispiel-Bank.
 * Von der passenden Job-Klasse ableiten, damit der Job gefunden wird.
 */
public class ExampleSynchronizeJobKontoauszug extends SynchronizeJobKontoauszug implements ExampleSynchronizeJob
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  @Resource
  private ExampleSynchronizeBackend backend = null;
  
  /**
   * @see org.jameica.hibiscus.sync.example.ExampleSynchronizeJob#execute()
   */
  @Override
  public void execute() throws Exception
  {
    Konto konto = (Konto) this.getContext(CTX_ENTITY); // wurde von ExampleSynchronizeJobProviderKontoauszug dort abgelegt
    
    // Hier koennen wir jetzt die Netzwerkverbindung zur Bank aufbauen, dort die
    // Kontoauszuege abrufen und in Hibiscus anlegen
    
    Logger.info("Rufe Umsätze ab für " + backend.getName());
    
    ////////////////
    String username = konto.getMeta(ExampleSynchronizeBackend.PROP_USERNAME,null);
    String password = konto.getMeta(ExampleSynchronizeBackend.PROP_PASSWORD,null);
    if (username == null || username.length() == 0)
      throw new ApplicationException(i18n.tr("Bitte geben Sie Ihren Benutzernamen in den Synchronisationsoptionen ein"));
    
    if (password == null || password.length() == 0)
      throw new ApplicationException(i18n.tr("Bitte geben Sie Ihr Passwort in den Synchronisationsoptionen ein"));

    Logger.info("username: " + username);
    ////////////////
    
    List<Umsatz> fetched = new ArrayList<Umsatz>();
    
    // ....
//    for (...)
//    {
//      // Umsatz-Objekt erstellen
//      Umsatz newUmsatz = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);
//      newUmsatz.setKonto(konto);
//      newUmsatz.setBetrag(...);
//      newUmsatz.setDatum(...);
//      newUmsatz.setGegenkontoBLZ(...); // das darf auch eine BIC sein
//      newUmsatz.setGegenkontoName(...);
//      newUmsatz.setGegenkontoNummer(...); // das darf auch eine IBAN sein
//      newUmsatz.setSaldo(...); // Zwischensaldo
//      newUmsatz.setValuta(...);
//      newUmsatz.setZweck(...);
//      newUmsatz.setZweck2(...);
//      newUmsatz.setWeitereVerwendungszwecke(...);
//      fetched.add(newUmsatz);
//    }
    
    // Dann muessen wir die jetzt noch mit den bereits in der Datenbank vorhandenen Umsaetzen
    // abgleichen, um nur die in der Datenbank zu speichern, die wir noch nicht haben

    Date oldest = null;
    
    // Ermitteln des aeltesten abgerufenen Umsatzes, um den Bereich zu ermitteln,
    // gegen den wir aus der Datenbank abgleichen
    for (Umsatz umsatz:fetched)
    {
      if (oldest == null || umsatz.getDatum().before(oldest))
        oldest = umsatz.getDatum();
    }
    
    
    // Wir holen uns die Umsaetze seit dem letzen Abruf von der Datenbank
    GenericIterator existing = konto.getUmsaetze(oldest,null);
    for (Umsatz umsatz:fetched)
    {
      if (existing.contains(umsatz) != null)
        continue; // haben wir schon
      
      // Neuer Umsatz. Anlegen
      umsatz.store();
      
      // Per Messaging Bescheid geben, dass es einen neuen Umsatz gibt. Der wird dann sofort in der Liste angezeigt
      Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
    }
    
    // Zum Schluss sollte noch der neue Saldo des Kontos sowie das Datum des Abrufes
    // im Konto gespeichert werden
    konto.setSaldo(150.00); // gegen sinnvollen Wert ersetzen ;)
    konto.store();
    
    // Und per Messaging Bescheid geben, dass das Konto einen neuen Saldo hat
    Application.getMessagingFactory().sendMessage(new SaldoMessage(konto));
  }
}


