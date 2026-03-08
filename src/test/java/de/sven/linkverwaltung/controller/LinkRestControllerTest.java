package de.sven.linkverwaltung.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sven.linkverwaltung.model.Link;
import de.sven.linkverwaltung.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LinkRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        linkRepository.deleteAll();
    }

    // 1. Ohne Login -> Redirect
    @Test
    void alleLinks_ohneLogin_redirect() throws Exception {
        mockMvc.perform(get("/api/links"))
                .andExpect(status().is3xxRedirection());
    }

    // 2. Mit Login -> 200 + JSON
    @Test
    void alleLinks_mitLogin_ok() throws Exception {
        mockMvc.perform(get("/api/links").with(user("sven")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // 3. Link anlegen -> 200 + neuer Link
    @Test
    void linkAnlegen_ok() throws Exception {
        String json = """
                {"url":"https://example.com","titel":"Test","channel":"TestChannel"}
                """;

        mockMvc.perform(post("/api/links")
                        .with(user("sven"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").value("https://example.com"))
                .andExpect(jsonPath("$.titel").value("Test"))
                .andExpect(jsonPath("$.channel").value("TestChannel"))
                .andExpect(jsonPath("$.erstelltAm").exists());

        assertEquals(1, linkRepository.count());
    }

    // 4. Link anlegen ohne URL -> 400 Bad Request
    @Test
    void linkAnlegen_ohneUrl_fehler() throws Exception {
        String json = """
                {"titel":"Test","channel":"TestChannel"}
                """;

        mockMvc.perform(post("/api/links")
                        .with(user("sven"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(0, linkRepository.count());
    }

    // 5. Link aktualisieren -> 200 + geänderte Daten
    @Test
    void linkAktualisieren_ok() throws Exception {
        Link link = new Link();
        link.setUrl("https://example.com");
        link.setTitel("Alt");
        link.setChannel("AltChannel");
        link = linkRepository.save(link);

        String json = """
                {"url":"https://neu.com","titel":"Neu","channel":"NeuChannel"}
                """;

        mockMvc.perform(put("/api/links/" + link.getId())
                        .with(user("sven"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://neu.com"))
                .andExpect(jsonPath("$.titel").value("Neu"))
                .andExpect(jsonPath("$.channel").value("NeuChannel"));
    }

    // 6. Link aktualisieren mit falscher ID -> 404
    @Test
    void linkAktualisieren_nichtGefunden() throws Exception {
        String json = """
                {"url":"https://example.com","titel":"Test","channel":"Test"}
                """;

        mockMvc.perform(put("/api/links/99999")
                        .with(user("sven"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    // 7. Link loeschen -> 204
    @Test
    void linkLoeschen_ok() throws Exception {
        Link link = new Link();
        link.setUrl("https://example.com");
        link = linkRepository.save(link);

        mockMvc.perform(delete("/api/links/" + link.getId())
                        .with(user("sven")))
                .andExpect(status().isNoContent());

        assertEquals(0, linkRepository.count());
    }

    // 8. Link loeschen mit falscher ID -> 404
    @Test
    void linkLoeschen_nichtGefunden() throws Exception {
        mockMvc.perform(delete("/api/links/99999")
                        .with(user("sven")))
                .andExpect(status().isNotFound());
    }

    // 9. CSV Export -> text/csv mit Header
    @Test
    void csvExport_ok() throws Exception {
        Link link = new Link();
        link.setUrl("https://example.com");
        link.setTitel("CSV Test");
        link.setChannel("TestChannel");
        linkRepository.save(link);

        mockMvc.perform(get("/api/links/export/csv").with(user("sven")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=links.csv"))
                .andExpect(content().string(containsString("ID;URL;Titel;Channel;Erstellt am")))
                .andExpect(content().string(containsString("https://example.com")))
                .andExpect(content().string(containsString("CSV Test")));
    }

    // 10. Suche findet Link
    @Test
    void suche_findetLink() throws Exception {
        Link link1 = new Link();
        link1.setUrl("https://youtube.com/video1");
        link1.setTitel("Java Tutorial");
        link1.setChannel("TechChannel");
        linkRepository.save(link1);

        Link link2 = new Link();
        link2.setUrl("https://example.com");
        link2.setTitel("Anderes Thema");
        link2.setChannel("Sonstiges");
        linkRepository.save(link2);

        // Suche nach "Java" -> nur 1 Treffer
        mockMvc.perform(get("/api/links?suche=Java").with(user("sven")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titel").value("Java Tutorial"));

        // Suche nach "example" -> nur 1 Treffer (URL-Suche)
        mockMvc.perform(get("/api/links?suche=example").with(user("sven")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].url").value("https://example.com"));
    }
}
