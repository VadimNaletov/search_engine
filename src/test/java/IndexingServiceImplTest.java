import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexingServiceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IndexingServiceImplTest {

    @InjectMocks
    private IndexingServiceImpl indexingService;

    @Mock
    private SiteRepository siteRepository;
    @Mock
    private PageRepository pageRepository;
    @Mock
    private LemmaRepository lemmaRepository;
    @Mock
    private IndexRepository indexRepository;
    @Mock
    private SitesList sitesList;


    @Test
    void testStartIndexing() {
        boolean result = indexingService.startIndexing();
        assertTrue(result);
    }

    @Test
    void testStopIndexing() {
        SiteEntity siteEntity1 = new SiteEntity();
        siteEntity1.setUrl("http://www.example.com");
        siteEntity1.setStatus(StatusType.INDEXING);
        SiteEntity siteEntity2 = new SiteEntity();
        siteEntity2.setUrl("http://www.another-example.com");
        siteEntity2.setStatus(StatusType.INDEXED);

        List<SiteEntity> siteEntityList = new ArrayList<>();
        siteEntityList.add(siteEntity1);
        siteEntityList.add(siteEntity2);

        when(siteRepository.findAll()).thenReturn(siteEntityList);
        when(siteRepository.save(any(SiteEntity.class))).thenReturn(siteEntity1).thenReturn(siteEntity2);

        boolean result = indexingService.stopIndexing();

        assertTrue(result);
        assertEquals(StatusType.FAILED, siteEntity1.getStatus());
        assertEquals(StatusType.INDEXED, siteEntity2.getStatus());
    }

    @Test
    void testIndexPage() {

        List<SiteEntity> siteEntityList = new ArrayList<>();
        SiteEntity siteEntity1 = new SiteEntity();
        siteEntity1.setUrl("http://example1.com");
        siteEntity1.setName("Example 1");
        siteEntity1.setStatus(StatusType.FAILED);
        siteEntity1.setStatusTime(new Date());
        siteEntityList.add(siteEntity1);

        when(siteRepository.findAll()).thenReturn(siteEntityList);
        when(siteRepository.findByUrl("http://example1.com")).thenReturn(siteEntity1);

        String url = "http://example1.com/page1";

        boolean result = indexingService.indexPage(url);

        Assertions.assertTrue(result);
        Assertions.assertEquals(StatusType.INDEXED, siteEntity1.getStatus());
    }
}