package com.hlinks.domain.course.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillWeightNormalizer {

    private static final int WEIGHT_SCALE = 6;

    private SkillWeightNormalizer() {
    }

    public static BigDecimal resolveRawWeight(BigDecimal weight) {
        if (weight == null) {
            return BigDecimal.ONE;
        }

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return weight;
    }

    public static Map<Long, BigDecimal> normalizeToOne(Map<Long, BigDecimal> weights) {
        BigDecimal totalWeight = weights.values().stream()
                .filter(weight -> weight != null && weight.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (weights.isEmpty() || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of();
        }

        Map<Long, BigDecimal> normalizedWeights = new LinkedHashMap<>();
        List<Map.Entry<Long, BigDecimal>> entries = weights.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        BigDecimal accumulatedWeight = BigDecimal.ZERO;

        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<Long, BigDecimal> entry = entries.get(index);
            BigDecimal normalizedWeight;

            if (index == entries.size() - 1) {
                normalizedWeight = BigDecimal.ONE.subtract(accumulatedWeight);
            } else {
                normalizedWeight = entry.getValue().divide(totalWeight, WEIGHT_SCALE, RoundingMode.HALF_UP);
                accumulatedWeight = accumulatedWeight.add(normalizedWeight);
            }

            normalizedWeights.put(entry.getKey(), normalizedWeight.max(BigDecimal.ZERO));
        }

        return normalizedWeights;
    }
}
