package com.pocketempire.tech;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TechTree {

    public boolean isAvailable(String techId, Set<String> researchedTechs) {
        TechnologyConfig tech = TechConfigLoader.getConfig(techId);
        if (tech == null) return false;
        return researchedTechs.containsAll(tech.getPrerequisites());
    }

    public List<TechnologyConfig> getAvailable(Set<String> researchedTechs) {
        return TechConfigLoader.getAll().stream()
                .filter(tech -> !researchedTechs.contains(tech.getId()))
                .filter(tech -> isAvailable(tech.getId(), researchedTechs))
                .collect(Collectors.toList());
    }

    public boolean isUnitUnlocked(String requiredTech, Set<String> researchedTechs) {
        return requiredTech == null || researchedTechs.contains(requiredTech);
    }

    public boolean isBuildingUnlocked(String requiredTech, Set<String> researchedTechs) {
        return requiredTech == null || researchedTechs.contains(requiredTech);
    }
}
