import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import searchengine.Indexing;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IndexingTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SitesList sitesList;

    @InjectMocks
    private Indexing indexing;

    @Test
    public void testStopIndexingAllSites() {
        boolean result = indexing.stopIndexingAllSites();
        assertFalse(result);
    }

    @Test
    public void testAllSiteIndexing() throws InterruptedException {
        List<Site> siteList = new ArrayList<>();
        Site site1 = new Site();
        site1.setName("Site 1");
        site1.setUrl("www.site1.com");
        siteList.add(site1);

        Site site2 = new Site();
        site2.setName("Site 2");
        site2.setUrl("www.site2.com");
        siteList.add(site2);

        when(sitesList.getSites()).thenReturn(siteList);

        SiteEntity siteEntity1 = new SiteEntity();
        siteEntity1.setName("Site 1");
        siteEntity1.setUrl("site1.com");
        siteEntity1.setStatus(StatusType.FAILED);
        siteEntity1.setStatusTime(new Date());

        SiteEntity siteEntity2 = new SiteEntity();
        siteEntity2.setName("Site 2");
        siteEntity2.setUrl("site2.com");
        siteEntity2.setStatus(StatusType.FAILED);
        siteEntity2.setStatusTime(new Date());

        when(siteRepository.findByUrl("site1.com")).thenReturn(siteEntity1);
        when(siteRepository.findByUrl("site2.com")).thenReturn(siteEntity2);

        boolean result = indexing.allSiteIndexing();
        assertTrue(result);
    }

    @Test
    public void testStartSiteIndexing() {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setUrl("www.example.com");
        siteEntity.setName("Example");
        siteEntity.setStatus(StatusType.FAILED);
        siteEntity.setStatusTime(new Date());
        when(siteRepository.findByUrl("www.example.com")).thenReturn(siteEntity);

        boolean result = indexing.startSiteIndexing(siteEntity);
        assertTrue(result);
    }
}
