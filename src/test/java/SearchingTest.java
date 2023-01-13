import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import searchengine.Searching;
import searchengine.dto.search.SearchData;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchingTest {

    @Mock
    private IndexRepository indexRepository;
    @Mock
    private LemmaRepository lemmaRepository;
    @Mock
    private PageRepository pageRepository;
    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private Searching searching;

    @Test
    public void testGetSearchData() throws IOException {
        // given
        String query = "test";
        String url = "www.test.com";
        int offset = 0;
        int limit = 10;
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setId(1L);
        siteEntity.setUrl("www.test.com");
        when(siteRepository.findAll()).thenReturn(Collections.singletonList(siteEntity));
        when(siteRepository.findByUrl("test.com")).thenReturn(siteEntity);
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setId(1L);
        lemmaEntity.setLemma("test");
        lemmaEntity.setFrequency(1);
        lemmaEntity.setSiteId(1L);
        SearchData searchData = searching.getSearchData(query, url, offset, limit);
        assertEquals(0, searchData.getCount());
        assertEquals(Collections.emptyList(), searchData.getDetailedSearchData());
    }
}