package com.adventurersguild.quest;

import com.adventurersguild.AdventurersGuild;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A data-driven quest definition. Loaded from data/adventurersguild/quests/*.json
 * and synced to clients for display. All state transitions are validated server-side.
 */
public class Quest {
    private final String id;
    private final QuestType type;
    private final QuestQuality quality;
    private final String titleKey;
    private final String descriptionKey;
    /** COLLECT: item id or "#tag"; HUNT: entity type id. */
    private final String target;
    private final int amount;
    /** Type-specific extra data: EXPLORE/SURVIVE biome or condition; TRANSPORT "x,z". */
    private final String extra;
    /** TRANSPORT delivery radius (blocks). */
    private final int radius;
    private final int goldReward;
    private final int expReward;
    private final int reputationReward;
    private final int recommendedLevel;
    private final int minLevel;
    private final int minReputation;
    private final int timeLimitSeconds;
    private final boolean tutorial;

    public Quest(String id, QuestType type, QuestQuality quality, String titleKey, String descriptionKey,
                 String target, int amount, String extra, int radius,
                 int goldReward, int expReward, int reputationReward,
                 int recommendedLevel, int minLevel, int minReputation,
                 int timeLimitSeconds, boolean tutorial) {
        this.id = id;
        this.type = type;
        this.quality = quality;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.target = target;
        this.amount = amount;
        this.extra = extra;
        this.radius = radius;
        this.goldReward = goldReward;
        this.expReward = expReward;
        this.reputationReward = reputationReward;
        this.recommendedLevel = recommendedLevel;
        this.minLevel = minLevel;
        this.minReputation = minReputation;
        this.timeLimitSeconds = timeLimitSeconds;
        this.tutorial = tutorial;
    }

    public static Quest fromJson(ResourceLocation file, JsonObject json) {
        String id = json.has("id") ? json.get("id").getAsString() : file.getPath();
        QuestType type = QuestType.byName(json.has("type") ? json.get("type").getAsString() : null);
        QuestQuality quality = QuestQuality.byName(json.has("quality") ? json.get("quality").getAsString() : null);
        String titleKey = json.has("title")
                ? json.get("title").getAsString()
                : "quest." + AdventurersGuild.MOD_ID + "." + id + ".title";
        String descriptionKey = json.has("description")
                ? json.get("description").getAsString()
                : "quest." + AdventurersGuild.MOD_ID + "." + id + ".desc";

        JsonObject objective = json.getAsJsonObject("objective");
        if (objective == null) {
            throw new IllegalArgumentException("Quest '" + id + "' is missing 'objective'");
        }
        String target = objective.has("target") ? objective.get("target").getAsString() : "";
        int amount = objective.has("amount") ? objective.get("amount").getAsInt() : 0;
        String extra = objective.has("extra") ? objective.get("extra").getAsString() : "";
        int radius = objective.has("radius") ? objective.get("radius").getAsInt() : 64;
        boolean targetRequired = type != QuestType.SURVIVE;
        if (targetRequired && target.isEmpty()) {
            throw new IllegalArgumentException("Quest '" + id + "' has an empty objective target");
        }
        boolean amountRequired = type != QuestType.EXPLORE;
        if (amountRequired && amount <= 0) {
            throw new IllegalArgumentException("Quest '" + id + "' has an invalid amount: " + amount);
        }

        JsonObject reward = json.getAsJsonObject("reward");
        int gold = reward != null && reward.has("gold") ? reward.get("gold").getAsInt() : 0;
        int exp = reward != null && reward.has("exp") ? reward.get("exp").getAsInt() : 0;
        int reputation = reward != null && reward.has("reputation") ? reward.get("reputation").getAsInt() : 0;
        int recommendedLevel = json.has("recommended_level") ? json.get("recommended_level").getAsInt() : 1;
        int minLevel = json.has("min_level") ? json.get("min_level").getAsInt() : 1;
        int minReputation = json.has("min_reputation") ? json.get("min_reputation").getAsInt() : 0;
        int timeLimitSeconds = json.has("time_limit_seconds") ? json.get("time_limit_seconds").getAsInt() : 1800;
        boolean tutorial = json.has("tutorial") && json.get("tutorial").getAsBoolean();

        return new Quest(id, type, quality, titleKey, descriptionKey, target, amount, extra, radius,
                gold, exp, reputation, recommendedLevel, minLevel, minReputation, timeLimitSeconds, tutorial);
    }

    /** True when the picked-up item advances this COLLECT quest. */
    public boolean matchesItem(ItemStack stack) {
        if (type != QuestType.COLLECT || stack.isEmpty()) {
            return false;
        }
        if (target.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(target.substring(1));
            return tagId != null && stack.is(TagKey.create(Registries.ITEM, tagId));
        }
        ResourceLocation itemId = ResourceLocation.tryParse(target);
        if (itemId == null) {
            return false;
        }
        return BuiltInRegistries.ITEM.getOptional(itemId).map(h -> stack.is(h)).orElse(false);
    }

    /** True when the killed entity advances this HUNT quest. */
    public boolean matchesEntity(EntityType<?> entityType) {
        if (type != QuestType.HUNT) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(target);
        if (id == null) {
            return false;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(type -> type == entityType).orElse(false);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(type.name());
        buf.writeUtf(quality.name());
        buf.writeUtf(titleKey);
        buf.writeUtf(descriptionKey);
        buf.writeUtf(target);
        buf.writeVarInt(amount);
        buf.writeUtf(extra);
        buf.writeVarInt(radius);
        buf.writeVarInt(goldReward);
        buf.writeVarInt(expReward);
        buf.writeVarInt(reputationReward);
        buf.writeVarInt(recommendedLevel);
        buf.writeVarInt(minLevel);
        buf.writeVarInt(minReputation);
        buf.writeVarInt(timeLimitSeconds);
        buf.writeBoolean(tutorial);
    }

    public static Quest decode(FriendlyByteBuf buf) {
        String id = buf.readUtf(128);
        QuestType type = QuestType.byName(buf.readUtf(32));
        QuestQuality quality = QuestQuality.byName(buf.readUtf(32));
        String titleKey = buf.readUtf(256);
        String descriptionKey = buf.readUtf(512);
        String target = buf.readUtf(128);
        int amount = buf.readVarInt();
        String extra = buf.readUtf(128);
        int radius = buf.readVarInt();
        int gold = buf.readVarInt();
        int exp = buf.readVarInt();
        int reputation = buf.readVarInt();
        int recommendedLevel = buf.readVarInt();
        int minLevel = buf.readVarInt();
        int minReputation = buf.readVarInt();
        int timeLimitSeconds = buf.readVarInt();
        boolean tutorial = buf.readBoolean();
        return new Quest(id, type, quality, titleKey, descriptionKey, target, amount, extra, radius,
                gold, exp, reputation, recommendedLevel, minLevel, minReputation, timeLimitSeconds, tutorial);
    }

    public String getId() { return id; }
    public QuestType getType() { return type; }
    public QuestQuality getQuality() { return quality; }
    public String getTitleKey() { return titleKey; }
    public String getDescriptionKey() { return descriptionKey; }
    public String getTarget() { return target; }
    public int getAmount() { return amount; }
    public String getExtra() { return extra; }
    public int getRadius() { return radius; }
    public int getGoldReward() { return goldReward; }
    public int getExpReward() { return expReward; }
    public int getReputationReward() { return reputationReward; }
    public int getRecommendedLevel() { return recommendedLevel; }
    public int getMinLevel() { return minLevel; }
    public int getMinReputation() { return minReputation; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public boolean isTutorial() { return tutorial; }
}
