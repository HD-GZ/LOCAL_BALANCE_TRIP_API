package live.lbtrip.domain.tourism.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.entity.CategoryGroupCode;
import live.lbtrip.domain.tourism.model.enums.CodeLevel;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.vo.CategoryGroupMapping;
import live.lbtrip.domain.tourism.repository.CategoryGroupCodeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryGroupClassifier {

    private final CategoryGroupCodeRepository categoryGroupCodeRepository;

    public CategoryGroupMapping load() {
        Map<String, Set<CategoryGroup>> byCat1 = new HashMap<>();
        Map<String, Set<CategoryGroup>> byCat2 = new HashMap<>();
        Map<String, Set<CategoryGroup>> byCat3 = new HashMap<>();
        for (CategoryGroupCode mapping : categoryGroupCodeRepository.findAll()) {
            Map<String, Set<CategoryGroup>> target = switch (mapping.getCodeLevel()) {
                case CAT1 -> byCat1;
                case CAT2 -> byCat2;
                case CAT3 -> byCat3;
            };
            target.computeIfAbsent(mapping.getCode(), key -> new HashSet<>())
                .add(mapping.getCategoryGroup());
        }
        return new CategoryGroupMapping(byCat1, byCat2, byCat3);
    }
}
