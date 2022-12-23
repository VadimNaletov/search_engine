package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import searchengine.Searching;
import searchengine.dto.search.SearchResponse;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    private final Searching searching;
    @Override
    public SearchResponse getSearchData(String query, @Nullable String url, int offset, int limit) {
        SearchResponse searchResponse = new SearchResponse();
        if(query.equals("")){
            searchResponse.setResult(false);
            searchResponse.setError("Задан пустой поисковый запрос");
        } else if(url == null){
            try {
                searchResponse.setSearchData(searching.getSearchData(query, null, offset, limit));
            } catch (IOException e) {
                searchResponse.setResult(false);
                searchResponse.setError("Поисковый запрос не дал результатов");
                searchResponse.setSearchData(null);
            }
            searchResponse.setResult(true);
        } else {
            try {
                searchResponse.setSearchData(searching.getSearchData(query, null, offset, limit));
            } catch (IOException e) {
                searchResponse.setResult(false);
                searchResponse.setError("Поисковый запрос не дал результатов");
                searchResponse.setSearchData(null);
            }
        }
        return searchResponse;
    }
}
