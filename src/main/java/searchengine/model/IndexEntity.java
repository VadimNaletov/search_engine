package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "index_table")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(nullable = false, name = "page_id")
    private long pageId;
    @Column(nullable = false, name = "lemma_id")
    private long lemmaId;
    @Column(nullable = false, name = "rank_number")
    private float rank;
}
