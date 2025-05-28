package com.jewey.rosia.common.capabilities;

public class TimerCapability {

    public TimerCapability() {}


    public static Remainder stepTicks(long ticks, int burnTicks) {
        if ((long)burnTicks > ticks) {
            burnTicks = (int)((long)burnTicks - ticks);
            return new Remainder(burnTicks, 0L);
        } else {
            ticks -= burnTicks;
            burnTicks = 0;

            return new Remainder(burnTicks, ticks);
        }
    }

    public record Remainder(int burnTicks, long ticks) {
        public Remainder(int burnTicks, long ticks) {
            this.burnTicks = burnTicks;
            this.ticks = ticks;
        }

        public int burnTicks() {
            return this.burnTicks;
        }
        public long ticks() {
            return this.ticks;
        }
    }
}
