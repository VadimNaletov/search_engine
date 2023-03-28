package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {
    @Query(value = "SELECT * FROM search_engine.index_table where (:lemmaId IS NULL or lemma_id = :lemmaId) limit 1", nativeQuery = true)
    IndexEntity findIndexByLemmaId(long lemmaId);
    @Query(value = "SELECT * FROM search_engine.index_table where (:lemmaId IS NULL or lemma_id = :lemmaId)", nativeQuery = true)
    List<IndexEntity> getAllIndexesByLemmaId(long lemmaId);
    @Query(value = "SELECT SUM(e.rank_number) FROM search_engine.index_table e WHERE e.page_id = :pageId AND e.lemma_id = :lemmaId", nativeQuery = true)
    Float countAbsoluteRelevance(long pageId, long lemmaId);
}
