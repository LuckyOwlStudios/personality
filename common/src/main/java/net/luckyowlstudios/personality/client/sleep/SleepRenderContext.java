package net.luckyowlstudios.personality.client.sleep;

import java.util.ArrayDeque;

/**
 * Tracks the sleeping pose of the entity currently being rendered, so every model drawn for it
 * (the body, plus layers such as sheep wool, saddles and armor) gets the same lying-down pose.
 */
public final class SleepRenderContext {

    private static final ArrayDeque<State> STACK = new ArrayDeque<>();
    private static State current = State.AWAKE;

    private SleepRenderContext() {
    }

    public static void push(float progress, boolean foldLegs) {
        STACK.push(current);
        current = progress <= 0.0F ? State.AWAKE : new State(progress, foldLegs);
    }

    public static void pop() {
        current = STACK.isEmpty() ? State.AWAKE : STACK.pop();
    }

    public static State current() {
        return current;
    }

    public record State(float progress, boolean foldLegs) {
        static final State AWAKE = new State(0.0F, false);

        public boolean active() {
            return this.progress > 0.0F;
        }
    }
}
