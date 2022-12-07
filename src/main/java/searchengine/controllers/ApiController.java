package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.ResponseService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Object> startIndexing(){
        ResponseService responseService = indexingService.startIndexing();
        return ResponseEntity.ok(responseService);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing(){
        ResponseService responseService = indexingService.stopIndexing();
        if(!responseService.getResult()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Индексация не запущена");
        }
        return ResponseEntity.ok(responseService);
    }
    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexPage(@RequestParam String url){
        ResponseService responseService = indexingService.indexPage(url);
        if(!responseService.getResult()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return ResponseEntity.ok(responseService);
    }
}
