package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "pages", indexes = @Index(name = "path(255)", columnList = "path", unique = true))
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "site_id")
    private long siteId;
}
