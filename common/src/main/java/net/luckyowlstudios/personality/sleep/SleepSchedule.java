package net.luckyowlstudios.personality.sleep;

import net.luckyowlstudios.personality.config.PersonalityConfig;
import net.luckyowlstudios.personality.trait.TraitEffects;
import net.minecraft.world.entity.Mob;

/**
 * Works out when an individual mob should be asleep.
 * <p>
 * Every mob gets its own bedtime and wake-up time, derived from its UUID, so a herd drifts off
 * one by one instead of all at once, and each animal keeps the same habits after a reload.
 */
public final class SleepSchedule {

    private static final int DAY_LENGTH = 24000;

    // Bedtimes, wake-up times and spread come from PersonalityConfig ("schedule" section).
    // Defaults: day-active animals go to bed between 18:30 and ~21:00 and wake between ~04:50 and ~07:10;
    // night-active animals go to bed between 06:30 and ~09:00 and wake between 16:30 and ~18:50.

    private SleepSchedule() {
    }

    public static boolean isBedtime(Mob mob, long dayTime, boolean nocturnal) {
        long seed = mob.getUUID().getMostSignificantBits() ^ mob.getUUID().getLeastSignificantBits();
        long spread = Math.max(1L, Math.round(PersonalityConfig.SPREAD_HOURS.get() * 1000.0));
        int bedOffset = (int) Math.floorMod(mix(seed), spread);
        int wakeOffset = (int) Math.floorMod(mix(seed * 31L + 0x9E3779B97F4A7C15L), spread);

        // Personality traits push the whole schedule around (a silly animal stays up late, a sleepy one turns in early).
        int start = PersonalityConfig.dayTime(nocturnal ? PersonalityConfig.NOCTURNAL_BEDTIME : PersonalityConfig.DIURNAL_BEDTIME) + bedOffset + TraitEffects.bedtimeShift(mob);
        int end = PersonalityConfig.dayTime(nocturnal ? PersonalityConfig.NOCTURNAL_WAKE : PersonalityConfig.DIURNAL_WAKE) + wakeOffset + TraitEffects.wakeShift(mob);
        int time = (int) Math.floorMod(dayTime, (long) DAY_LENGTH);
        return inWindow(time, Math.floorMod(start, DAY_LENGTH), Math.floorMod(end, DAY_LENGTH));
    }

    private static boolean inWindow(int time, int start, int end) {
        return start <= end ? time >= start && time < end : time >= start || time < end;
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
