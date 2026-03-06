package de.sven.linkverwaltung.controller;

import de.sven.linkverwaltung.model.Link;
import de.sven.linkverwaltung.repository.LinkRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class LinkController {

    private final LinkRepository linkRepository;

    public LinkController(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @GetMapping
    public String index(@RequestParam(value = "suche", required = false) String suche, Model model) {
        List<Link> links;
        if (suche != null && !suche.isBlank()) {
            links = linkRepository.findByTitelContainingIgnoreCaseOrChannelContainingIgnoreCaseOrUrlContainingIgnoreCase(
                    suche, suche, suche);
        } else {
            links = linkRepository.findAll();
        }
        model.addAttribute("links", links);
        model.addAttribute("link", new Link());
        model.addAttribute("suche", suche);
        return "links";
    }

    @PostMapping("/speichern")
    public String speichern(@ModelAttribute Link link) {
        linkRepository.save(link);
        return "redirect:/";
    }

    @GetMapping("/bearbeiten/{id}")
    public String bearbeiten(@PathVariable Long id, Model model) {
        Link link = linkRepository.findById(id).orElseThrow();
        model.addAttribute("link", link);
        model.addAttribute("links", linkRepository.findAll());
        return "links";
    }

    @GetMapping("/loeschen/{id}")
    public String loeschen(@PathVariable Long id) {
        linkRepository.deleteById(id);
        return "redirect:/";
    }
}
