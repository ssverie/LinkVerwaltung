package de.sven.linkverwaltung.controller;

import de.sven.linkverwaltung.model.Link;
import de.sven.linkverwaltung.repository.LinkRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/links")
public class LinkRestController {

    private final LinkRepository linkRepository;

    public LinkRestController(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    // GET /api/links         -> alle Links
    // GET /api/links?suche=x -> Links durchsuchen
    @GetMapping
    public List<Link> alleLinks(@RequestParam(value = "suche", required = false) String suche) {
        if (suche != null && !suche.isBlank()) {
            return linkRepository.findByTitelContainingIgnoreCaseOrChannelContainingIgnoreCaseOrUrlContainingIgnoreCase(
                    suche, suche, suche);
        }
        return linkRepository.findAll();
    }

    // GET /api/links/5 -> einen Link
    @GetMapping("/{id}")
    public ResponseEntity<Link> einLink(@PathVariable Long id) {
        return linkRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/links -> neuen Link anlegen
    @PostMapping
    public ResponseEntity<?> anlegen(@RequestBody Link link) {
        if (link.getUrl() == null || link.getUrl().isBlank()) {
            return ResponseEntity.badRequest().body("URL ist erforderlich");
        }
        link.setId(null);
        return ResponseEntity.ok(linkRepository.save(link));
    }

    // PUT /api/links/5 -> Link aktualisieren
    @PutMapping("/{id}")
    public ResponseEntity<Link> aktualisieren(@PathVariable Long id, @RequestBody Link link) {
        return linkRepository.findById(id)
                .map(bestehend -> {
                    bestehend.setUrl(link.getUrl());
                    bestehend.setTitel(link.getTitel());
                    bestehend.setChannel(link.getChannel());
                    return ResponseEntity.ok(linkRepository.save(bestehend));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/links/export/csv -> alle Links als CSV
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        List<Link> links = linkRepository.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        StringBuilder csv = new StringBuilder();
        csv.append("ID;URL;Titel;Channel;Erstellt am\n");
        for (Link l : links) {
            csv.append(l.getId()).append(";");
            csv.append(escapeCsv(l.getUrl())).append(";");
            csv.append(escapeCsv(l.getTitel())).append(";");
            csv.append(escapeCsv(l.getChannel())).append(";");
            csv.append(l.getErstelltAm() != null ? l.getErstelltAm().format(fmt) : "").append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=links.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body("\uFEFF" + csv.toString()); // BOM fuer Excel-Kompatibilitaet
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // DELETE /api/links/5 -> Link loeschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> loeschen(@PathVariable Long id) {
        if (linkRepository.existsById(id)) {
            linkRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
