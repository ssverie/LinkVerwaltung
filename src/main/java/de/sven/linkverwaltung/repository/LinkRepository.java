package de.sven.linkverwaltung.repository;

import de.sven.linkverwaltung.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {

    List<Link> findByTitelContainingIgnoreCaseOrChannelContainingIgnoreCaseOrUrlContainingIgnoreCase(
            String titel, String channel, String url);
}
