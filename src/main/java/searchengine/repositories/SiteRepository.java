package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {
    @Query(value = "SELECT * FROM search_engine.sites where (:url IS NULL or url like :url)", nativeQuery = true)
    SiteEntity findByUrl(String url);
}
