package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    @Query(value = "SELECT * FROM search_engine.lemmas where (:lemma IS NULL or lemma like :lemma)", nativeQuery = true)
    LemmaEntity findByLemma(String lemma);
    @Query(value = "SELECT COUNT(*) FROM search_engine.lemmas", nativeQuery = true)
    Integer countLemmas();
    @Query(value = "SELECT COUNT(*) FROM search_engine.lemmas WHERE site_id = :siteId", nativeQuery = true)
    Integer countBySiteId(long siteId);
    @Query(value = "SELECT * FROM search_engine.lemmas WHERE (:lemma IS NULL or lemma like :lemma)", nativeQuery = true)
    List<LemmaEntity> getLemmasList(String lemma);
}
