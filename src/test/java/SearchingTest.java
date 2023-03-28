import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import searchengine.Searching;
import searchengine.dto.search.SearchData;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

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
        when(siteRepository.findByUrl("example.com")).thenReturn(new SiteEntity());
        Mockito.lenient().when(lemmaRepository.findByLemma("тест")).thenReturn(new LemmaEntity());
        Mockito.lenient().when(indexRepository.getAllIndexesByLemmaId(1)).thenReturn(new ArrayList<>());
        Mockito.lenient().when(pageRepository.findById(1L)).thenReturn(Optional.of(new PageEntity()));
        SearchData result = searching.getSearchData("тест", "example.com", 0, 1);
        assertNotNull(result);
        assertEquals(0, result.getCount());
        verify(siteRepository, times(1)).findByUrl("example.com");
    }
    @Test
    public void testGetSearchData_withUrl() throws IOException {
        when(siteRepository.findByUrl("example.com")).thenReturn(new SiteEntity());
        Mockito.lenient().when(lemmaRepository.findByLemma("тест")).thenReturn(new LemmaEntity());
        Mockito.lenient().when(indexRepository.getAllIndexesByLemmaId(1)).thenReturn(new ArrayList<>());
        Mockito.lenient().when(pageRepository.findById(1L)).thenReturn(Optional.of(new PageEntity()));

        SearchData result = searching.getSearchData("тест", "example.com", 0, 1);

        assertNotNull(result);
        assertEquals(0, result.getCount());
        verify(siteRepository, times(1)).findByUrl("example.com");
    }

    @Test
    public void testGetSearchData_withLimitAndOffset() throws IOException {
        String query = "test";
        String url = "https://example.com";
        int offset = 1;
        int limit = 3;

        when(siteRepository.findByUrl(url)).thenReturn(new SiteEntity());
        when(lemmaRepository.getLemmasList(query)).thenReturn(List.of(new LemmaEntity()));
        when(pageRepository.findById(anyLong())).thenReturn(Optional.of(new PageEntity()));
        when(indexRepository.getAllIndexesByLemmaId(anyLong())).thenReturn(List.of(new IndexEntity()));
        when(indexRepository.countAbsoluteRelevance(anyLong(), anyLong())).thenReturn(1f);

        SearchData searchData = searching.getSearchData(query, url, offset, limit);

        assertNotNull(searchData);
        assertNotNull(searchData.getDetailedSearchData());
        assertTrue(searchData.getDetailedSearchData().isEmpty());
        assertEquals(1, searchData.getCount());

        verify(siteRepository, times(1)).findByUrl(url);
        verify(lemmaRepository, times(1)).getLemmasList(query);
        verify(pageRepository, times(1)).findById(anyLong());
        verify(indexRepository, times(1)).getAllIndexesByLemmaId(anyLong());
        verify(indexRepository, times(1)).countAbsoluteRelevance(anyLong(), anyLong());
    }
}

