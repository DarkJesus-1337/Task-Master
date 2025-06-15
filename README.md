
# TaskManager

TaskManager ist eine Android-App zur Verwaltung von Aufgaben und ToDos. Sie ermöglicht es Benutzern, Aufgaben zu erstellen, zu bearbeiten, zu löschen und als erledigt zu markieren. Die App ist für den produktiven Alltag konzipiert und unterstützt eine einfache sowie intuitive Bedienung.

## Features

- Aufgaben erstellen, bearbeiten und löschen
- Aufgaben als erledigt markieren
- Übersichtliche Listenansicht aller Aufgaben
- Fälligkeitsdatum und Priorität für Aufgaben festlegen
- Lokale Speicherung der Aufgaben (z.B. mit Room oder SQLite)
- Responsive Benutzeroberfläche im Material Design
- (Optional) Benachrichtigungen für fällige Aufgaben

## Installation

1. **Repository klonen**
   ```bash
   git clone https://github.com/dein-benutzername/TaskManager.git
   ```
2. **Projekt in Android Studio öffnen**
3. **Abhängigkeiten synchronisieren**  
   Android Studio lädt automatisch alle benötigten Abhängigkeiten.
4. **App auf einem Emulator oder Gerät ausführen**

## Nutzung

- **Aufgabe hinzufügen:** Tippe auf das "+"-Symbol und fülle die Felder aus.
- **Aufgabe bearbeiten:** Tippe auf eine Aufgabe in der Liste und wähle "Bearbeiten".
- **Aufgabe löschen:** Wische eine Aufgabe nach links oder rechts oder nutze das Kontextmenü.
- **Als erledigt markieren:** Setze das Häkchen neben der Aufgabe.

## Architektur

- **MVVM (Model-View-ViewModel) Pattern**
- **Datenbank:** Room (SQLite)
- **UI:** Jetpack Compose oder klassische XML-Layouts
- **Dependency Injection:** (optional) Hilt oder Dagger
- **Weitere Libraries:** LiveData, ViewModel, RecyclerView, Material Components

## Beispiel-Code

```kotlin
// Aufgabe-Datenklasse
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val priority: Int,
    val isCompleted: Boolean = false
)
```

## Lizenz

Dieses Projekt steht unter der MIT-Lizenz. Weitere Informationen siehe [LICENSE](LICENSE).

## Kontakt

Bei Fragen oder Anregungen:  
Sebastian Dendtler  
sebastian.dendtler@email.de

