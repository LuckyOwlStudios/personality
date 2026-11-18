package net.luckyowlstudios.personality.retaliation;

import net.luckyowlstudios.personality.panic.HerdPanic;
import net.luckyowlstudios.personality.sleep.PersonalityTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Animals in the {@code personality:retaliates} tag (cows, mooshrooms and goats by default) fight back against a
 * player who hurts them: they charge the player and headbutt them instead of running away. Babies still flee.
 */
public final class Retaliation {

    /** Master switch, meant to be driven by the mod's config. */
    public static boolean enabled = true;

    /** How long an animal stays angry after the last hit, in ticks. */
    public static final int ANGRY_TICKS = 200;

    /** An angry animal gives up once the player is this far away, in blocks. */
    public static final double GIVE_UP_DISTANCE = 20.0;

    /** Speed multiplier while charging. */
    public static final float CHARGE_SPEED = 1.4F;

    /** Ticks between headbutts. */
    public static final int ATTACK_COOLDOWN = 25;

    /** Damage of a headbutt for animals without an attack damage attribute (cows), in half hearts. */
    public static final float DEFAULT_DAMAGE = 2.0F;

    /** Knockback of a cow's headbutt. Goats ram harder, see {@link #GOAT_KNOCKBACK}. */
    public static final double KNOCKBACK = 0.6;

    public static final double GOAT_KNOCKBACK = 1.2;

    private Retaliation() {
    }

    /**
     * Called on the server after {@code victim} was hurt by {@code player}, before {@link HerdPanic#alarm}, so an
     * animal that fights back doesn't also run away.
     */
    public static void provoke(LivingEntity victim, Player player) {
        if (victim instanceof PathfinderMob mob && canRetaliate(mob) && isValidTarget(mob, player)) {
            ((AngryMob) mob).personality$provoke(player, ANGRY_TICKS);
        }
    }

    /** Grown, wild, unleashed animals in the {@code personality:retaliates} tag. */
    public static boolean canRetaliate(PathfinderMob mob) {
        return enabled && mob.isAlive() && !mob.isNoAi() && !mob.isBaby() && !mob.isLeashed() && !mob.isVehicle()
                && mob.getType().is(PersonalityTags.RETALIATES);
    }

    /** Creative and spectator players can't be hurt, so animals don't bother with them. */
    public static boolean isValidTarget(PathfinderMob mob, LivingEntity target) {
        if (!target.isAlive() || target.level() != mob.level()
                || target.distanceToSqr(mob) > GIVE_UP_DISTANCE * GIVE_UP_DISTANCE) {
            return false;
        }
        return !(target instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }

    /** Rams {@code target}: some damage, a shove away from the animal and a sound. */
    public static void headbutt(PathfinderMob mob, LivingEntity target) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        boolean goat = mob.getType() == EntityType.GOAT;
        float damage = mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)
                ? (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE) : DEFAULT_DAMAGE;
        DamageSource source = mob.damageSources().mobAttack(mob);
        if (!target.hurt(source, damage)) {
            return;
        }
        double knockback = goat ? GOAT_KNOCKBACK : KNOCKBACK;
        double dx = mob.getX() - target.getX();
        double dz = mob.getZ() - target.getZ();
        if (dx * dx + dz * dz < 1.0E-4) {
            dx = -Math.sin(Math.toRadians(mob.getYRot()));
            dz = Math.cos(Math.toRadians(mob.getYRot()));
        }
        target.knockback(knockback, dx, dz);
        mob.setLastHurtMob(target);
        if (goat) {
            level.playSound(null, mob, SoundEvents.GOAT_RAM_IMPACT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else {
            mob.playAmbientSound();
        }
    }
}
