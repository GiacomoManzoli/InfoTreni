# Documentazione classi

## Activitys

- `MainActivity`: activity principale, contiene il drawer per visualizzare i vari fragment: 	
	- `TrainReminderStatusFragment`: lista dei reminder attivi, con le informazioni sullo stato del treno
	- `QuickSearchFragment`: permette la ricerca rapida di un treno mediante codice o tratta. -> Una volta ottenuti i risultati rimanda si sostituisce con `TrainStatusFragment`
	- `ManageReminderFragment`: permette di eseguire le operazioni di CRUD (no U) sui remined. Il pulsante Add fa partire una nuova activity `AddReminderActivity`.
	- `SettingsFragment`: permette di configurare l'applicazione (al momento solo le notifiche)

- `AddReminderActivity`: activity per l'aggiunta di un reminder.

	- `SelectTrainFragment`: permette la ricerca del treno da monitorare, renderizzando un `FindTrainFragment`. Quando è stato selezionato un treno, passa ad un `ConfigReminderFragment` per la configurazione e per il completamento dell'inserimento. **OMMMIODDDIO da cambiare assolutamente!** **CORRETTO**


### Refactor necessari

- **FATTO** Delegare a `AddReminderActivity` lo swap tra i due fragment, il che probabilmente renderà inutile `SelectTrainFragment`
- **FATTO** Modificare `QuickSearchFragment` in modo che una volta trovato il treno venga avviata una nuova activity `TrainStatusActivity`, in modo analogo a come avviene per la creazione di un nuovo reminder.



## Fragments

- `FindStationFragment`: *DialogFragment* che visualizza una lista filtrabile di stazioni
- `FindTrainFragment`: *DialogFragment* che visualizza i controlli necessari per la selezione di un treno: textbox per il codice oppure pulsanti per selezionare la stazione di Partenza/arrivo e l'orario di arrivo
- `TrainListFragment`: *DialogFragment* che visualizza una lista di treni, viene utilizzato per permettere all'utente di selezionare un treno quando ci sono più treni con lo stesso codice che fanno tratte diverse. **Grazie Viaggiatreno**
- `TrainStatusFragment`: *Fragment* che visualizza le infomrazioni di un treno (ritardo, partenza, ultimo rilevamento, stazione di riferiemento) **TODO: esplicitare che il treno è già arrivato/ridisegnare con più informazioni**


## Altri TODO

- Eventualmente modificare la gestione del DB SQLite in modo che sia simile a quella che usa lui negli esempi a lezione.

## Notifications

- `SchedulingAlarmRecevier`: receiver che viene notificato dal sistema operativo quando è ora di effettuare una nuova notifica.
- `TrainStatusSchedulingService`: background service che si preoccupa di creare le notifiche.
