package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "sites")
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private Date statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;
}
