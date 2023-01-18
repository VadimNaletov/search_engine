import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceImplTest {

    @Mock
    private SitesList sites;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private LemmaRepository lemmaRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    public void testGetStatistics() {
        // Set up mock data
        List<Site> sitesList = new ArrayList<>();
        Site site1 = new Site();
        site1.setName("Test Site 1");
        site1.setUrl("www.testsite1.com");
        sitesList.add(site1);
        Site site2 = new Site();
        site2.setName("Test Site 2");
        site2.setUrl("www.testsite2.com");
        sitesList.add(site2);

        when(sites.getSites()).thenReturn(sitesList);
        when(lemmaRepository.countLemmas()).thenReturn(100);
        when(pageRepository.countPages()).thenReturn(200);

        SiteEntity siteEntity1 = new SiteEntity();
        siteEntity1.setId(1L);
        siteEntity1.setUrl("testsite1.com");
        siteEntity1.setName("Test Site 1");
        siteEntity1.setStatus(StatusType.INDEXED);
        siteEntity1.setLastError("");
        when(siteRepository.findByUrl("testsite1.com")).thenReturn(siteEntity1);
        when(lemmaRepository.countBySiteId(1L)).thenReturn(50);
        when(pageRepository.countBySiteId(1L)).thenReturn(100);

        SiteEntity siteEntity2 = new SiteEntity();
        siteEntity2.setId(2L);
        siteEntity2.setUrl("testsite2.com");
        siteEntity2.setName("Test Site 2");
        siteEntity2.setStatus(StatusType.FAILED);
        siteEntity2.setLastError("Ошибка в процессе индексации");
        when(siteRepository.findByUrl("testsite2.com")).thenReturn(siteEntity2);
        when(lemmaRepository.countBySiteId(2L)).thenReturn(25);
        when(pageRepository.countBySiteId(2L)).thenReturn(50);

        StatisticsResponse result = statisticsService.getStatistics();

        assertTrue(result.isResult());
        StatisticsData data = result.getStatistics();
        TotalStatistics total = data.getTotal();
        assertEquals(2, total.getSites());
        assertTrue(total.isIndexing());
        assertEquals(100, total.getLemmas());
        assertEquals(200, total.getPages());
        List<DetailedStatisticsItem> detailed = data.getDetailed();
        assertEquals(2, detailed.size());
        DetailedStatisticsItem item1 = detailed.get(0);
        assertEquals("Test Site 1", item1.getName());
        assertEquals("testsite1.com", item1.getUrl());
        assertEquals(100, item1.getPages());
        assertEquals(50, item1.getLemmas());
        assertEquals(StatusType.INDEXED, item1.getStatus());
        assertEquals("", item1.getError());
        DetailedStatisticsItem item2 = detailed.get(1);
        assertEquals("Test Site 2", item2.getName());
        assertEquals("testsite2.com", item2.getUrl());
        assertEquals(50, item2.getPages());
        assertEquals(25, item2.getLemmas());
        assertEquals(StatusType.FAILED, item2.getStatus());
        assertEquals("Ошибка в процессе индексации", item2.getError());
    }
    @Test
    public void testGetStatisticsWithNullSite(){
        List<Site> sitesList = new ArrayList<>();
        Site site1 = new Site();
        site1.setName("Test Site 1");
        site1.setUrl("www.testsite1.com");
        sitesList.add(site1);
        Site site2 = new Site();
        site2.setName("Test Site 2");
        site2.setUrl("www.testsite2.com");
        sitesList.add(site2);

        when(sites.getSites()).thenReturn(sitesList);
        when(lemmaRepository.countLemmas()).thenReturn(100);
        when(pageRepository.countPages()).thenReturn(200);

        SiteEntity siteEntity1 = new SiteEntity();
        siteEntity1.setId(1L);
        siteEntity1.setUrl("testsite1.com");
        siteEntity1.setName("Test Site 1");
        siteEntity1.setStatus(StatusType.INDEXED);
        siteEntity1.setLastError("");
        when(siteRepository.findByUrl("testsite1.com")).thenReturn(siteEntity1);
        when(lemmaRepository.countBySiteId(1L)).thenReturn(50);
        when(pageRepository.countBySiteId(1L)).thenReturn(100);

        when(siteRepository.findByUrl("testsite2.com")).thenReturn(null);

        StatisticsResponse result = statisticsService.getStatistics();

        assertTrue(result.isResult());
        StatisticsData data = result.getStatistics();
        TotalStatistics total = data.getTotal();
        assertEquals(2, total.getSites());
        assertTrue(total.isIndexing());
        assertEquals(100L, total.getLemmas());
        assertEquals(200L, total.getPages());
        List<DetailedStatisticsItem> detailed = data.getDetailed();
        assertEquals(2, detailed.size());
        DetailedStatisticsItem item2 = detailed.get(1);
        assertEquals("Test Site 2", item2.getName());
        assertEquals("testsite2.com", item2.getUrl());
        assertEquals(0, item2.getPages());
        assertEquals(0, item2.getLemmas());
        assertEquals(StatusType.FAILED, item2.getStatus());
        assertEquals("Индексация еще не запускалась", item2.getError());
    }

    @Test
    public void testGetStatisticsWithNoSites() {
        when(sites.getSites()).thenReturn(new ArrayList<>());
        StatisticsResponse result = statisticsService.getStatistics();
        StatisticsData data = result.getStatistics();
        TotalStatistics total = data.getTotal();
        assertEquals(0, total.getSites());
        assertEquals(0, total.getPages());
        List<DetailedStatisticsItem> detailed = data.getDetailed();
        assertEquals(0, detailed.size());
    }
}


