package searchengine.dto.search;

import lombok.Data;

import java.util.List;
@Data
public class SearchData {
    private int count;
    private List<DetailedSearchData> detailedSearchData;
}
