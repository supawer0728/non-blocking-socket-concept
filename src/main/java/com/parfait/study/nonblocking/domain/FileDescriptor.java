package com.parfait.study.nonblocking.domain;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FileDescriptor {

    private static final int START_INDEX = 3;
    private static final AtomicInteger CLASS_SEQUENCE = new AtomicInteger(START_INDEX);
    private final int value;

    FileDescriptor() {
        this.value = CLASS_SEQUENCE.getAndIncrement();
    }

    // Object Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileDescriptor)) {
            return false;
        }
        FileDescriptor that = (FileDescriptor) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
