# Architektur

## Technologie-Stack

| Komponente | Technologie | Version |
|------------|-------------|---------|
| Sprache | Java | 21 |
| Build-Tool | Maven | 3.6+ |
| Framework | Spring Boot | 3.4.3 |
| Web | Spring Web + Thymeleaf | - |
| Datenbank-Zugriff | Spring Data JPA (Hibernate) | - |
| DB-Migration | Flyway | 10.x |
| Datenbanken | H2 (embedded), PostgreSQL | 2.x / 14-17 |
| Frontend | Bootstrap (CDN) | 5.3.3 |

## Projektstruktur

```
C:/Projekte/LinkVerwaltung/
|
|-- pom.xml                              Maven-Konfiguration
|-- docker-compose.yml                   Docker PostgreSQL Container
|
|-- docs/
|   |-- SETUP.md                         Setup-Anleitung
|   |-- PROFILE.md                       Profile-Dokumentation
|   |-- ARCHITEKTUR.md                   Dieses Dokument
|
|-- src/main/java/de/sven/linkverwaltung/
|   |-- LinkVerwaltungApplication.java   Spring Boot Main-Klasse
|   |-- model/
|   |   |-- Link.java                    JPA Entity
|   |-- repository/
|   |   |-- LinkRepository.java          Spring Data Repository
|   |-- controller/
|       |-- LinkController.java          Web Controller (CRUD)
|
|-- src/main/resources/
|   |-- application.properties           Gemeinsame Konfiguration
|   |-- application-h2.properties        H2-Profil
|   |-- application-postgres.properties  Docker-Profil
|   |-- application-wsl.properties       WSL2-Profil
|   |-- templates/
|   |   |-- links.html                   Thymeleaf Template
|   |-- db/migration/
|       |-- h2/
|       |   |-- V1__create_links_table.sql
|       |-- postgresql/
|           |-- V1__create_links_table.sql
|
|-- src/test/java/de/sven/linkverwaltung/
|   |-- LinkVerwaltungApplicationTests.java
|
|-- data/                                H2-Datenbankdatei (nicht in Git)
|-- target/                              Build-Ausgabe (nicht in Git)
```

## Datenmodell

### Tabelle: links

| Spalte | Typ (H2) | Typ (PostgreSQL) | Nullable | Beschreibung |
|--------|----------|-------------------|----------|--------------|
| id | BIGINT AUTO_INCREMENT | BIGSERIAL | Nein | Primaerschluessel |
| url | VARCHAR(2048) | VARCHAR(2048) | Nein | Die URL |
| titel | VARCHAR(500) | VARCHAR(500) | Ja | Titel des Links |
| channel | VARCHAR(255) | VARCHAR(255) | Ja | Channel/Quelle |
| erstellt_am | TIMESTAMP | TIMESTAMP | Ja | Erstellungszeitpunkt (automatisch) |

### JPA Entity: Link.java

```java
@Entity
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;          // Pflichtfeld

    private String titel;        // Optional
    private String channel;      // Optional

    @Column(name = "erstellt_am")
    private LocalDateTime erstelltAm;  // Wird automatisch gesetzt (@PrePersist)
}
```

## Schichten-Architektur

```
Browser (http://localhost:808x)
    |
    v
LinkController.java          @Controller - nimmt HTTP-Requests entgegen
    |                         GET  /              -> Liste + Formular
    |                         POST /speichern     -> Link speichern
    |                         GET  /bearbeiten/1  -> Link ins Formular laden
    |                         GET  /loeschen/1    -> Link loeschen
    |
    v
LinkRepository.java          @Repository - Spring Data JPA
    |                         findAll()
    |                         findById(id)
    |                         save(link)
    |                         deleteById(id)
    |                         findByTitel...OrChannel...OrUrl...(suche)
    |
    v
H2 / PostgreSQL              Datenbank (je nach Profil)
```

## Flyway Datenbank-Migrationen

Flyway verwaltet das Datenbank-Schema. Hibernate erstellt KEINE Tabellen (`ddl-auto=validate`).

### Verzeichnisstruktur

```
src/main/resources/db/migration/
|-- h2/
|   |-- V1__create_links_table.sql       H2-spezifisches SQL
|-- postgresql/
    |-- V1__create_links_table.sql       PostgreSQL-spezifisches SQL
```

### Namenskonvention

```
V{nummer}__{beschreibung}.sql
```

- `V` = Versioned Migration
- `{nummer}` = Versionsnummer (1, 2, 3, ...)
- `__` = Doppelter Unterstrich (Pflicht!)
- `{beschreibung}` = Was die Migration macht

### Beispiel: Neue Migration hinzufuegen

Wenn z.B. ein Feld `kategorie` hinzugefuegt werden soll:

**H2:** `db/migration/h2/V2__add_kategorie_to_links.sql`
```sql
ALTER TABLE links ADD COLUMN kategorie VARCHAR(255);
```

**PostgreSQL:** `db/migration/postgresql/V2__add_kategorie_to_links.sql`
```sql
ALTER TABLE links ADD COLUMN kategorie VARCHAR(255);
```

Dann die Entity `Link.java` um das Feld erweitern:
```java
private String kategorie;
// + getter und setter
```

Flyway fuehrt jede Migration genau einmal aus und speichert den Status in der Tabelle `flyway_schema_history`.

### Wichtig

- Einmal ausgefuehrte Migrationen NIEMALS aendern!
- Immer neue Migrationen mit hoeherer Nummer anlegen
- Bei beiden Datenbanken (h2/ und postgresql/) die Migration anlegen
- Nach Aenderung: JAR neu bauen und nach WSL2 kopieren

## Weboberflaeche

Eine einzige Seite (`links.html`) mit:

1. **Formular** (oben) - URL, Titel, Channel eingeben + Speichern
2. **Suchfeld** - Durchsucht URL, Titel und Channel
3. **Tabelle** - Alle Links mit klickbarer URL, Bearbeiten- und Loeschen-Button

### Template-Engine: Thymeleaf

Die HTML-Datei liegt unter `src/main/resources/templates/links.html`.
Thymeleaf ersetzt Platzhalter (`th:text`, `th:each`, etc.) mit den Daten aus dem Controller.

### CSS-Framework: Bootstrap 5

Bootstrap wird per CDN geladen (kein lokaler Download noetig):
- CSS: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css`
- JS: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js`

## Git

### Commits

```
cd46650  Initial commit: LinkVerwaltung Spring Boot App
617c3aa  Add Flyway for database migration management
d118fce  Add PostgreSQL support with Spring Profiles
6a71fea  Add WSL2 profile and separate ports per environment
```

### .gitignore

Folgende Dateien/Ordner sind NICHT im Repository:

| Pfad | Grund |
|------|-------|
| `target/` | Build-Ausgabe |
| `data/` | H2-Datenbankdatei |
| `*.class` | Kompilierte Klassen |
| `*.jar` | Build-Artefakte |
| `.idea/`, `*.iml` | IntelliJ IDEA |
| `.vscode/` | VS Code |

### Repository

GitHub: **https://github.com/ssverie/LinkVerwaltung**
