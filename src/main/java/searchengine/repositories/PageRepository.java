package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {
    @Query(value = "SELECT * FROM search_engine.pages where (:path IS NULL or path like :path)", nativeQuery = true)
    PageEntity findByPath(String path);
    @Query(value = "SELECT COUNT(*) FROM search_engine.pages", nativeQuery = true)
    Integer countPages();
    @Query(value = "SELECT COUNT(*) FROM search_engine.pages WHERE site_id = :siteId", nativeQuery = true)
    Integer countBySiteId(long siteId);

}
