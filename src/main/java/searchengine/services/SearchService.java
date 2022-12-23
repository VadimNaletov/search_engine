package searchengine.services;

import searchengine.dto.search.SearchResponse;

public interface SearchService {

    SearchResponse getSearchData(String query, String url, int offset, int limit);
}
