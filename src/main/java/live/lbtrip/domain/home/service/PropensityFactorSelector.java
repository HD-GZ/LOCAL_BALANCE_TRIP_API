package live.lbtrip.domain.home.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.home.model.PropensityFactor;

@Component
public class PropensityFactorSelector {

    private static final int SELECT_COUNT = 3;

    private final Random random;

    public PropensityFactorSelector() {
        this(new Random());
    }

    PropensityFactorSelector(Random random) {
        this.random = random;
    }

    public List<PropensityFactor> selectThree() {
        List<PropensityFactor> all = new ArrayList<>(List.of(PropensityFactor.values()));
        Collections.shuffle(all, random);
        return List.copyOf(all.subList(0, SELECT_COUNT));
    }
}
