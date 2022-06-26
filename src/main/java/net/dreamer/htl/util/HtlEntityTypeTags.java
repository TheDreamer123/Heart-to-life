package net.dreamer.htl.util;

import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HtlEntityTypeTags {
    public static final TagKey<EntityType<?>> CONVERTIBLE_UNDEAD = TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier("c", "convertible_undead"));
}
