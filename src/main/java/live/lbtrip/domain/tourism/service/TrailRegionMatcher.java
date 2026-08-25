package live.lbtrip.domain.tourism.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.global.util.StringNormalizer;

public class TrailRegionMatcher {

    private static final Map<String, String> PROVINCE_ALIASES = Map.of(
        "강원도", "강원특별자치도",
        "전라북도", "전북특별자치도",
        "제주도", "제주특별자치도"
    );

    private final Map<String, RegionCandidate> candidatesByKey = new HashMap<>();

    public TrailRegionMatcher(List<RegionCandidate> candidates) {
        for (RegionCandidate candidate : candidates) {
            keyOf(candidate.getName()).ifPresent(key -> candidatesByKey.put(key, candidate));
        }
    }

    public Optional<RegionCandidate> match(String sigun) {
        return keyOf(sigun).map(candidatesByKey::get);
    }

    private Optional<String> keyOf(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return Optional.empty();
        }
        String[] tokens = StringNormalizer.trim(regionName).split("\\s+");
        if (tokens.length < 2) {
            return Optional.empty();
        }
        String province = PROVINCE_ALIASES.getOrDefault(tokens[0], tokens[0]);
        return Optional.of(province + " " + tokens[tokens.length - 1]);
    }
}
