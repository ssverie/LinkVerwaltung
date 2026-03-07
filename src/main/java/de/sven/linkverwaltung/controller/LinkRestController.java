package de.sven.linkverwaltung.controller;

import de.sven.linkverwaltung.model.Link;
import de.sven.linkverwaltung.repository.LinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Link anlegen(@RequestBody Link link) {
        link.setId(null); // sicherstellen, dass neu angelegt wird
        return linkRepository.save(link);
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
