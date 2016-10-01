/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.core.service.streamid;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Set;
import java.util.function.BiPredicate;

import cern.streaming.pool.core.service.StreamId;

public class BufferSpecification {

    private StreamId<?> startId;
    private Set<EndStreamMatcher<?, ?>> endStreamMatchers;
    private Duration timeout;

    private static final Duration NO_TIMEOUT = Duration.ofSeconds(-1);

    private BufferSpecification(StreamId<?> startStreamId, Set<EndStreamMatcher<?, ?>> endStreamMatchers,
            Duration timeout) {
        this.startId = requireNonNull(startStreamId, "startStreamId must not be null.");
        this.endStreamMatchers = requireNonNull(endStreamMatchers, "endStreamId must not be null.");
        this.timeout = requireNonNull(timeout, "timeout must not be null");
    }

    public static BufferSpecification ofStartEndTimeout(StreamId<?> startStreamId,
            Set<EndStreamMatcher<?, ?>> endStreamMatchers, Duration timeout) {
        return new BufferSpecification(startStreamId, endStreamMatchers, timeout);
    }

    public static BufferSpecification ofStartAndEnd(StreamId<?> startStreamId,
            Set<EndStreamMatcher<?, ?>> endStreamMatchers) {
        return new BufferSpecification(startStreamId, endStreamMatchers, NO_TIMEOUT);
    }

    public StreamId<?> startId() {
        return startId;
    }

    public Set<EndStreamMatcher<?, ?>> endStreamMatchers() {
        return endStreamMatchers;
    }

    public Duration timeout() {
        return timeout;
    }

    public static class EndStreamMatcher<T, U> {
        private final StreamId<U> endStreamId;
        private final BiPredicate<T, U> matching;

        public EndStreamMatcher(StreamId<U> endStreamId, BiPredicate<T, U> matching) {
            super();
            this.endStreamId = endStreamId;
            this.matching = matching;
        }

        public static final <T, U> EndStreamMatcher<T, U> ofStreamMatching(StreamId<U> endStreamId,
                BiPredicate<T, U> matching) {
            return new EndStreamMatcher<>(endStreamId, matching);
        }

        public static final <U> EndStreamMatcher<?, U> alwaysEndingOn(StreamId<U> endStreamId) {
            return ofStreamMatching(endStreamId, (a, b) -> true);
        }

        public StreamId<U> endStreamId() {
            return this.endStreamId;
        }

        public BiPredicate<T, U> matching() {
            return this.matching;
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endStreamMatchers == null) ? 0 : endStreamMatchers.hashCode());
        result = prime * result + ((startId == null) ? 0 : startId.hashCode());
        result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BufferSpecification other = (BufferSpecification) obj;
        if (endStreamMatchers == null) {
            if (other.endStreamMatchers != null) {
                return false;
            }
        } else if (!endStreamMatchers.equals(other.endStreamMatchers)) {
            return false;
        }
        if (startId == null) {
            if (other.startId != null) {
                return false;
            }
        } else if (!startId.equals(other.startId)) {
            return false;
        }
        if (timeout == null) {
            if (other.timeout != null) {
                return false;
            }
        } else if (!timeout.equals(other.timeout)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BufferSpecification [startId=" + startId + ", endStreamMatchers=" + endStreamMatchers + ", timeout="
                + timeout + "]";
    }

}