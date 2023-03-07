package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing(){
        IndexingResponse indexingResponse = indexingService.startIndexing();
//        if(!indexingResponse.isResult()){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(indexingResponse);
//        }
        return ResponseEntity.ok(indexingResponse);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing(){
        IndexingResponse indexingResponse = indexingService.stopIndexing();
//        if(!indexingResponse.isResult()){
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(indexingResponse);
//        }
        return ResponseEntity.ok(indexingResponse);
    }
    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam String url){
        IndexingResponse indexingResponse = indexingService.indexPage(url);
//        if(!indexingResponse.isResult()){
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(indexingResponse);
//        }
        return ResponseEntity.ok(indexingResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query, @Nullable String url, int offset, int limit){
        SearchResponse searchResponse = searchService.getSearchData(query, url, offset, limit);
        return ResponseEntity.ok(searchResponse);
    }
}
