package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<Object> startIndexing(){
        boolean response = indexingService.startIndexing();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing(){
        boolean response = indexingService.stopIndexing();
        if(!response){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Индексация не запущена");
        }
        return ResponseEntity.ok(true);
    }
    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexPage(@RequestParam String url){
        boolean response = indexingService.indexPage(url);
        if(!response){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(String query, @Nullable String url, int offset, int limit){
        SearchResponse searchResponse = searchService.getSearchData(query, url, offset, limit);
        return ResponseEntity.ok(searchResponse);
    }
}
