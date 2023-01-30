import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.ResponseService;
import searchengine.services.FalseResponseService;
import searchengine.services.TrueResponseService;
import searchengine.Indexing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IndexingServiceImplTest {

    @InjectMocks
    private IndexingServiceImpl indexingServiceImpl;

    @Mock
    private Indexing indexing;

    @Test
    public void testStartIndexing_success() throws InterruptedException {
        when(indexing.allSiteIndexing()).thenReturn(true);
        ResponseService response = indexingServiceImpl.startIndexing();
        assertEquals(response.getClass(), TrueResponseService.class);
        verify(indexing, times(1)).allSiteIndexing();
    }

    @Test
    public void testStartIndexing_failure() throws InterruptedException {
        when(indexing.allSiteIndexing()).thenThrow(InterruptedException.class);
        ResponseService response = indexingServiceImpl.startIndexing();
        assertEquals(response.getClass(), FalseResponseService.class);
        verify(indexing, times(1)).allSiteIndexing();
    }

    @Test
    public void testStopIndexing_success() {
        when(indexing.stopIndexingAllSites()).thenReturn(true);
        ResponseService response = indexingServiceImpl.stopIndexing();
        assertEquals(response.getClass(), TrueResponseService.class);
        verify(indexing, times(1)).stopIndexingAllSites();
    }

    @Test
    public void testStopIndexing_failure() {
        when(indexing.stopIndexingAllSites()).thenReturn(false);
        ResponseService response = indexingServiceImpl.stopIndexing();
        assertEquals(response.getClass(), FalseResponseService.class);
        verify(indexing, times(1)).stopIndexingAllSites();
    }

    @Test
    public void testIndexPage_success() {
        String url = "www.example.com";
        when(indexing.startOneSiteIndexing(url)).thenReturn(true);
        ResponseService response = indexingServiceImpl.indexPage(url);
        assertEquals(response.getClass(), TrueResponseService.class);
        verify(indexing, times(1)).startOneSiteIndexing(url);
    }

    @Test
    public void testIndexPage_failure() {
        String url = "www.example.com";
        when(indexing.startOneSiteIndexing(url)).thenReturn(false);
        ResponseService response = indexingServiceImpl.indexPage(url);
        assertEquals(response.getClass(), FalseResponseService.class);
        verify(indexing, times(1)).startOneSiteIndexing(url);
    }
}