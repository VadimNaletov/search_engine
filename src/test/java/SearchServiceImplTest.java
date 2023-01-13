import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import searchengine.Searching;
import searchengine.dto.search.DetailedSearchData;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.services.SearchServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceImplTest{
    @Mock
    private Searching searching;

    @InjectMocks
    private SearchServiceImpl searchService;

    @Test
    @DisplayName("Пустая поисковая строка")
    public void testGetSearchData_EmptyQuery() {
        SearchResponse searchResponse = searchService.getSearchData("", null, 0, 0);
        assertFalse(searchResponse.isResult());
        assertEquals("Задан пустой поисковый запрос", searchResponse.getError());
    }

    @Test
    @DisplayName("URL сайта не задан")
    public void testGetSearchData_NullUrl() throws IOException {
        SearchData searchData = new SearchData();
        searchData.setCount(1);
        DetailedSearchData detailedSearchData = new DetailedSearchData();
        detailedSearchData.setUri("/test/1");
        detailedSearchData.setRelevance(1);
        detailedSearchData.setSiteName("Example");
        detailedSearchData.setTitle("Example site");
        detailedSearchData.setSnippet("test");
        List<DetailedSearchData> detailedSearchDataList = new ArrayList<>();
        detailedSearchDataList.add(detailedSearchData);
        searchData.setDetailedSearchData(detailedSearchDataList);
        when(searching.getSearchData("test", null, 0, 0)).thenReturn(searchData);
        SearchResponse searchResponse = searchService.getSearchData("test", null, 0, 0);
        assertTrue(searchResponse.isResult());
        assertNull(searchResponse.getError());
        assertEquals(searchData, searchResponse.getSearchData());
    }
}