package net.minecraft.advancements.critereon;

import java.util.Optional;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * 1.20.1 base class of trigger instances (criterion id + player predicate). 1.20.5 made instances records
 * implementing {@code SimpleCriterionTrigger.SimpleInstance}; this is that, for old instances.
 */
public abstract class AbstractCriterionTriggerInstance implements SimpleCriterionTrigger.SimpleInstance {
    private final Identifier criterion;
    private final ContextAwarePredicate player;

    public AbstractCriterionTriggerInstance(Identifier criterion, ContextAwarePredicate player) {
        this.criterion = criterion;
        this.player = player;
    }

    public Identifier getCriterion() {
        return criterion;
    }

    protected ContextAwarePredicate getPlayerPredicate() {
        return player;
    }

    /** Player conditions of old instances aren't translated; they match any player. */
    @Override
    public Optional<Holder<LootItemCondition>> player() {
        return Optional.empty();
    }
}
