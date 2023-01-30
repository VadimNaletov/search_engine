package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import searchengine.Searching;
import searchengine.dto.search.SearchResponse;

import java.io.IOException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    private static final Logger logger = LogManager.getLogger(SearchServiceImpl.class);

    private final Searching searching;
    public SearchResponse getSearchData(String query, @Nullable String url, int offset, int limit) {
        SearchResponse searchResponse = new SearchResponse();

        if (isQueryEmpty(query, searchResponse)) {
            return searchResponse;
        }

        if (url == null) {
            handleSearchWithoutUrl(query, offset, limit, searchResponse);
        } else {
            handleSearchWithUrl(query, url, offset, limit, searchResponse);
        }

        return searchResponse;
    }

    private boolean isQueryEmpty(String query, SearchResponse searchResponse) {
        if(Objects.equals(query, "")){
            searchResponse.setResult(false);
            searchResponse.setError("Задан пустой поисковый запрос");
            return true;
        }
        return false;
    }

    private void handleSearchWithoutUrl(String query, int offset, int limit, SearchResponse searchResponse) {
        try {
            searchResponse.setSearchData(searching.getSearchData(query, null, offset, limit));
        } catch (IOException e) {
            setErrorInSearchResponse(searchResponse);
            logger.error("Could not get SearchData - " + e.getMessage());
        }
        searchResponse.setResult(true);
    }

    private void handleSearchWithUrl(String query, String url, int offset, int limit, SearchResponse searchResponse) {
        try {
            searchResponse.setSearchData(searching.getSearchData(query, url, offset, limit));
        } catch (IOException e) {
            setErrorInSearchResponse(searchResponse);
            logger.error("Could not get SearchData - " + e.getMessage());
        }
    }

    private void setErrorInSearchResponse(SearchResponse searchResponse) {
        searchResponse.setResult(false);
        searchResponse.setError("Поисковый запрос не дал результатов");
        searchResponse.setSearchData(null);
    }

}
