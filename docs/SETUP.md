# Setup-Anleitung

## Voraussetzungen

| Software | Version | Pruefbefehl |
|----------|---------|-------------|
| Java JDK | 21+ | `java -version` |
| Maven | 3.6+ | `mvn -version` |
| Docker Desktop | (optional, fuer PostgreSQL Docker) | `docker --version` |
| WSL2 + Ubuntu | (optional, fuer Prod-Simulation) | `wsl --list` |

## Schnellstart (H2 - einfachste Variante)

Keine Datenbank-Installation noetig. H2 laeuft eingebettet.

```bash
cd C:/Projekte/LinkVerwaltung
mvn spring-boot:run
```

Fertig. Browser oeffnen: **http://localhost:8083**

## Starten mit Docker-PostgreSQL

### 1. Docker-Container starten

```bash
cd C:/Projekte/LinkVerwaltung
docker compose up -d
```

Das startet einen PostgreSQL 17 Container mit folgenden Daten:

| Einstellung | Wert |
|-------------|------|
| Container-Name | `link-postgres` |
| Image | `postgres:17` |
| Host-Port | `5556` |
| DB-Name | `linkverwaltung` |
| User | `sven` |
| Passwort | `sven123` |

### 2. App starten

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Browser oeffnen: **http://localhost:8084**

### 3. Docker-Container stoppen

```bash
docker compose down
```

Die Daten bleiben erhalten (Docker Volume `link-pg-data`).

## Starten mit WSL2-PostgreSQL (Prod-Simulation)

### 1. PostgreSQL in WSL2 einrichten (einmalig)

Terminal oeffnen und in WSL2 wechseln:

```bash
wsl -d Ubuntu-22.04
```

Datenbank und User anlegen:

```bash
sudo -u postgres psql
```

```sql
CREATE USER sven WITH PASSWORD 'sven123';
CREATE DATABASE linkverwaltung OWNER sven;
\q
```

Verbindung testen:

```bash
psql -U sven -d linkverwaltung -c "SELECT current_database(), current_user;"
```

Erwartete Ausgabe:

```
 current_database | current_user
------------------+--------------
 linkverwaltung   | sven
```

### 2. JAR bauen und nach WSL2 kopieren

Auf Windows:

```bash
cd C:/Projekte/LinkVerwaltung
mvn clean package -DskipTests
```

In WSL2 kopieren:

```bash
wsl -d Ubuntu-22.04 -- bash -c "mkdir -p /home/sven/linkverwaltung && cp /mnt/c/Projekte/LinkVerwaltung/target/linkverwaltung-1.0.0.jar /home/sven/linkverwaltung/"
```

### 3. App in WSL2 starten

```bash
wsl -d Ubuntu-22.04
cd /home/sven/linkverwaltung
java -jar linkverwaltung-1.0.0.jar --spring.profiles.active=wsl
```

Browser oeffnen: **http://localhost:8085**

### 4. App im Hintergrund starten (optional)

```bash
wsl -d Ubuntu-22.04 -- bash << 'EOF'
cd /home/sven/linkverwaltung
setsid java -jar linkverwaltung-1.0.0.jar --spring.profiles.active=wsl </dev/null >/tmp/lv.log 2>&1 &
echo "Gestartet mit PID $!"
EOF
```

Logs pruefen:

```bash
wsl -d Ubuntu-22.04 -- tail -f /tmp/lv.log
```

## Alle 3 gleichzeitig starten

Alle Umgebungen koennen parallel laufen, da jede einen eigenen Port hat:

```bash
# Terminal 1: H2
mvn spring-boot:run

# Terminal 2: Docker
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# Terminal 3: WSL2
wsl -d Ubuntu-22.04
cd /home/sven/linkverwaltung
java -jar linkverwaltung-1.0.0.jar --spring.profiles.active=wsl
```

| Profil | URL |
|--------|-----|
| H2 | http://localhost:8083 |
| Docker | http://localhost:8084 |
| WSL2 | http://localhost:8085 |

## Stoppen

| Umgebung | Befehl |
|----------|--------|
| H2 / Docker-App | `Ctrl+C` im Terminal |
| Docker-Container | `docker compose down` |
| WSL2-App | `Ctrl+C` oder `kill <PID>` |

## H2-Console (nur H2-Profil)

Die H2-Datenbank hat eine eingebaute Web-Console:

- URL: **http://localhost:8083/h2-console**
- JDBC URL: `jdbc:h2:file:./data/linkverwaltung`
- User: `sa`
- Passwort: (leer)
