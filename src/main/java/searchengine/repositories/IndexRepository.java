package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {
    @Query(value = "SELECT COUNT(page_id) FROM search_engine.index_table where lemma_id = :lemmaId", nativeQuery = true)
    int countFrequencyByLemmaId(long lemmaId);
    @Query(value = "SELECT * FROM search_engine.index_table where (:lemmaId IS NULL or lemma_id = :lemma_id)", nativeQuery = true)
    IndexEntity findIndexByLemmaId(long lemmaId);
    @Query(value = "SELECT * FROM search_engine.index_table where (:lemmaId IS NULL or lemma_id = :lemmaId)", nativeQuery = true)
    List<IndexEntity> getAllIndexesByLemmaId(long lemmaId);
    @Query(value = "SELECT * FROM search_engine.index_table where (page_id = :pageId AND lemma_id = :lemmaId)", nativeQuery = true)
    Optional<IndexEntity> findIndexByLemmaIdAndPageId(long pageId, long lemmaId);
}
