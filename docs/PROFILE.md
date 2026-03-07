# Spring Profile

Die Anwendung unterstuetzt 3 Profile. Jedes Profil verbindet sich mit einer anderen Datenbank und laeuft auf einem eigenen Port.

## Uebersicht

| Profil | Datei | DB | DB-Port | App-Port | Zweck |
|--------|-------|----|---------|----------|-------|
| `h2` | `application-h2.properties` | H2 (eingebettet) | - | **8083** | Lokale Entwicklung |
| `postgres` | `application-postgres.properties` | Docker PostgreSQL 17 | 5556 | **8084** | Docker-Entwicklung |
| `wsl` | `application-wsl.properties` | WSL2 PostgreSQL 14 | 5432 | **8085** | Prod-Simulation |

Das Default-Profil ist `h2` (gesetzt in `application.properties`).

## Gemeinsame Konfiguration

Datei: `src/main/resources/application.properties`

```properties
spring.application.name=LinkVerwaltung
spring.profiles.active=h2                    # Default-Profil
spring.jpa.hibernate.ddl-auto=validate       # Schema wird von Flyway verwaltet
spring.jpa.show-sql=false
spring.flyway.enabled=true
```

Diese Einstellungen gelten fuer ALLE Profile. Profil-spezifische Werte ueberschreiben sie.

## Profil: h2

Datei: `src/main/resources/application-h2.properties`

```properties
server.port=8083
spring.flyway.locations=classpath:db/migration/h2
spring.datasource.url=jdbc:h2:file:./data/linkverwaltung
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

| Einstellung | Wert |
|-------------|------|
| Datenbank-Datei | `./data/linkverwaltung.mv.db` |
| User | `sa` |
| Passwort | (leer) |
| H2-Console | http://localhost:8083/h2-console |
| Flyway-Migrationen | `src/main/resources/db/migration/h2/` |

**Starten:**

```bash
mvn spring-boot:run
```

## Profil: postgres

Datei: `src/main/resources/application-postgres.properties`

```properties
server.port=8084
spring.flyway.locations=classpath:db/migration/postgresql
spring.datasource.url=jdbc:postgresql://localhost:5556/linkverwaltung
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=sven
spring.datasource.password=sven123
spring.h2.console.enabled=false
```

| Einstellung | Wert |
|-------------|------|
| Docker-Container | `link-postgres` |
| Image | `postgres:17` |
| Host-Port | `5556` |
| DB-Name | `linkverwaltung` |
| User | `sven` |
| Passwort | `sven123` |
| Flyway-Migrationen | `src/main/resources/db/migration/postgresql/` |

**Starten:**

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Profil: wsl

Datei: `src/main/resources/application-wsl.properties`

```properties
server.port=8085
spring.flyway.locations=classpath:db/migration/postgresql
spring.datasource.url=jdbc:postgresql://localhost:5432/linkverwaltung
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=sven
spring.datasource.password=sven123
spring.h2.console.enabled=false
```

| Einstellung | Wert |
|-------------|------|
| PostgreSQL-Version | 14 (auf WSL2 Ubuntu 22.04) |
| Host-Port | `5432` (Standard) |
| DB-Name | `linkverwaltung` |
| User | `sven` |
| Passwort | `sven123` |
| JAR-Speicherort | `/home/sven/linkverwaltung/` |
| Flyway-Migrationen | `src/main/resources/db/migration/postgresql/` |

**Starten (in WSL2):**

```bash
cd /home/sven/linkverwaltung
java -jar linkverwaltung-1.0.0.jar --spring.profiles.active=wsl
```

## Profil-Wechsel

Das aktive Profil wird so bestimmt (Prioritaet von oben nach unten):

1. Kommandozeile: `--spring.profiles.active=postgres`
2. Maven-Parameter: `-Dspring-boot.run.profiles=postgres`
3. Default in `application.properties`: `spring.profiles.active=h2`

## Datenbank-Zugangsdaten Zusammenfassung

| Profil | Host | Port | DB-Name | User | Passwort |
|--------|------|------|---------|------|----------|
| h2 | (eingebettet) | - | linkverwaltung | sa | (leer) |
| postgres | localhost | 5556 | linkverwaltung | sven | sven123 |
| wsl | localhost | 5432 | linkverwaltung | sven | sven123 |
