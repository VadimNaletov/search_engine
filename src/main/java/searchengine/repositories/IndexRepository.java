package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {
    @Modifying
    @Query(value = "DELETE FROM search_engine.index_table where page_id = :pageId", nativeQuery = true)
    void deleteByPageId(long pageId);
    @Query(value = "SELECT COUNT(page_id) FROM search_engine.index_table where lemma_id = :lemmaId", nativeQuery = true)
    int countFrequencyByLemmaId(long lemmaId);
}
