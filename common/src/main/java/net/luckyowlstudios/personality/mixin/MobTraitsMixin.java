package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.config.PersonalityConfig;
import net.luckyowlstudios.personality.sleep.PersonalityTags;
import net.luckyowlstudios.personality.trait.Trait;
import net.luckyowlstudios.personality.trait.TraitEffects;
import net.luckyowlstudios.personality.trait.TraitHolder;
import net.luckyowlstudios.personality.trait.Traits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Mob.class)
public abstract class MobTraitsMixin extends LivingEntity implements TraitHolder {

    /** Trait ids, comma separated. Synced so clients can show or react to traits. */
    @Unique
    private static final EntityDataAccessor<String> PERSONALITY$TRAITS = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.STRING);

    @Unique
    private static final String PERSONALITY$TRAITS_KEY = "personality:traits";

    /** True once this animal has had its traits rolled, so an animal born with none keeps none. */
    @Unique
    private boolean personality$traitsRolled;

    @Unique
    private String personality$cachedRaw = "";

    @Unique
    private Set<Trait> personality$cachedTraits = Set.of();

    protected MobTraitsMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void personality$defineTraitData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PERSONALITY$TRAITS, "");
    }

    @Override
    public Set<Trait> personality$getTraits() {
        String raw = this.entityData.get(PERSONALITY$TRAITS);
        if (!raw.equals(this.personality$cachedRaw)) {
            Set<Trait> traits = new LinkedHashSet<>();
            for (String id : raw.split(",")) {
                ResourceLocation location = id.isEmpty() ? null : ResourceLocation.tryParse(id);
                Trait trait = location == null ? null : Traits.get(location);
                if (trait != null) {
                    traits.add(trait);
                }
            }
            this.personality$cachedRaw = raw;
            this.personality$cachedTraits = Set.copyOf(traits);
        }
        return this.personality$cachedTraits;
    }

    @Override
    public void personality$setTraits(Collection<Trait> traits) {
        this.personality$traitsRolled = true;
        this.entityData.set(PERSONALITY$TRAITS, traits.stream().map(Trait::toString).distinct().collect(Collectors.joining(",")));
        TraitEffects.applyAttributes((Mob) (Object) this);
    }

    /** Animals meet their personality on their first server tick, whether freshly spawned, bred or from an old world. */
    @Inject(method = "tick", at = @At("HEAD"))
    private void personality$rollTraits(CallbackInfo ci) {
        if (!this.personality$traitsRolled && !this.level().isClientSide && PersonalityConfig.TRAITS_ENABLED.get()) {
            this.personality$traitsRolled = true;
            if (this.getType().is(PersonalityTags.HAS_TRAITS)) {
                this.personality$setTraits(Traits.roll(this.getRandom()));
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void personality$saveTraits(CompoundTag tag, CallbackInfo ci) {
        if (!this.personality$traitsRolled) {
            return;
        }
        ListTag list = new ListTag();
        for (Trait trait : this.personality$getTraits()) {
            list.add(StringTag.valueOf(trait.toString()));
        }
        tag.put(PERSONALITY$TRAITS_KEY, list);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void personality$loadTraits(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains(PERSONALITY$TRAITS_KEY, Tag.TAG_LIST)) {
            return;
        }
        Set<Trait> traits = new LinkedHashSet<>();
        for (Tag entry : tag.getList(PERSONALITY$TRAITS_KEY, Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getAsString());
            Trait trait = id == null ? null : Traits.get(id);
            if (trait != null) {
                traits.add(trait);
            }
        }
        this.personality$setTraits(traits);
    }
}
