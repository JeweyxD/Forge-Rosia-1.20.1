package com.jewey.rosia.common.capabilities;

import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * From Immersive Engineering:
 * https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18.2/src/main/java/blusunrize/immersiveengineering/common/util/ResettableCapability.java
 */

public final class ResettableCapability<T> {
    private final T containedValue;
    private final List<Runnable> onReset = new ArrayList<>();
    private LazyOptional<T> currentOptional = LazyOptional.empty();

    public ResettableCapability(T containedValue) {
        this.containedValue = containedValue;
    }

    public LazyOptional<T> getLO() {
        if (!currentOptional.isPresent()) currentOptional = constantOptional(containedValue);
        return currentOptional;
    }

    public T get() {
        return containedValue;
    }

    public <A> LazyOptional<A> cast() {
        return getLO().cast();
    }

    public void reset() {
        currentOptional.invalidate();
        this.onReset.forEach(Runnable::run);
    }

    public void addResetListener(Runnable onReset) {
        this.onReset.add(onReset);
    }

    private static <T> LazyOptional<T> constantOptional(T val) {
        LazyOptional<T> result = LazyOptional.of(() -> Objects.requireNonNull(val));
        // Resolve directly: There is currently a bug in the LO resolve code that can cause problems in multithreaded
        // contexts ("resolved" is set to a reference to null during the resolution on one thread, any other thread
        // trying to access the value will get null)
        result.resolve();
        return result;
    }
}
