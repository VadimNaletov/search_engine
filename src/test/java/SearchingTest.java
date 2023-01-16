import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import searchengine.Searching;
import searchengine.dto.search.SearchData;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
        when(siteRepository.findAll()).thenReturn(new ArrayList<>());
        when(siteRepository.findByUrl("example.com")).thenReturn(new SiteEntity());
        Mockito.lenient().when(lemmaRepository.findByLemma("тест")).thenReturn(new LemmaEntity());
        Mockito.lenient().when(indexRepository.getAllIndexesByLemmaId(1)).thenReturn(new ArrayList<>());
        Mockito.lenient().when(pageRepository.findById(1L)).thenReturn(Optional.of(new PageEntity()));
        SearchData result = searching.getSearchData("тест", "example.com", 0, 1);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getCount());
        verify(siteRepository, times(1)).findAll();
        verify(siteRepository, times(1)).findByUrl("example.com");
    }
    @Test
    public void testGetSearchData_withUrl() throws IOException {
        //arrange
        when(siteRepository.findByUrl("example.com")).thenReturn(new SiteEntity());
        Mockito.lenient().when(lemmaRepository.findByLemma("тест")).thenReturn(new LemmaEntity());
        Mockito.lenient().when(indexRepository.getAllIndexesByLemmaId(1)).thenReturn(new ArrayList<>());
        Mockito.lenient().when(pageRepository.findById(1L)).thenReturn(Optional.of(new PageEntity()));

        //act
        SearchData result = searching.getSearchData("тест", "example.com", 0, 1);

        //assert
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getCount());
        verify(siteRepository, times(1)).findByUrl("example.com");
    }

    @Test
    public void testGetSearchData_withLimitAndOffset() throws IOException {
        //arrange
        when(siteRepository.findAll()).thenReturn(new ArrayList<>());
        Mockito.lenient().when(lemmaRepository.findByLemma("тест")).thenReturn(new LemmaEntity());
        Mockito.lenient().when(indexRepository.getAllIndexesByLemmaId(1)).thenReturn(new ArrayList<>());
        Mockito.lenient().when(pageRepository.findById(1L)).thenReturn(Optional.of(new PageEntity()));

        //act
        SearchData result = searching.getSearchData("тест", null, 1, 1);

        //assert
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getCount());
        verify(siteRepository, times(1)).findAll();
    }
}