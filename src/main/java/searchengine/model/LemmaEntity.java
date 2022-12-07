package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "lemmas")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(nullable = false, name = "site_id")
    private long siteId;
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String lemma;
    @Column(nullable = false)
    private int frequency;
}
