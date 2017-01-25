// @formatter:off
/**
*
* This file is part of streaming pool (http://www.streamingpool.org).
* 
* Copyright (c) 2017-present, CERN. All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
*/
// @formatter:on

package cern.streaming.pool.core.service.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.streaming.pool.core.service.CycleInStreamDiscoveryDetectedException;
import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;

/**
 * Special implementation of a {@link DiscoveryService}. It is able to discover streams recursively while preventing
 * cycles. Also, it is able to detect recursive discoveries from multiple threads, which is not allowed.
 */
public class TrackKeepingDiscoveryService implements DiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackKeepingDiscoveryService.class);

    private final Set<StreamId<?>> idsOfStreamsUnderCreation;
    private final List<StreamFactory> factories;
    private final PoolContent content;
    private final Thread contextOfExecution;

    public TrackKeepingDiscoveryService(List<StreamFactory> factories, PoolContent content) {
        this(factories, content, new HashSet<>(), Thread.currentThread());
    }

    public TrackKeepingDiscoveryService(List<StreamFactory> factories, PoolContent content,
            Set<StreamId<?>> idsOfStreamsUnderCreation, Thread contextOfExecution) {
        this.factories = requireNonNull(factories, "factories must not be null");
        this.content = requireNonNull(content, "activeStreams must not be null");
        this.idsOfStreamsUnderCreation = Collections.unmodifiableSet(idsOfStreamsUnderCreation);
        this.contextOfExecution = requireNonNull(contextOfExecution, "contextOfExecution must not be null");
    }

    @Override
    public <T> Publisher<T> discover(StreamId<T> id) {
        checkSameContexOfExecution();
        checkForRecursiveCycles(id);

        content.synchronousPutIfAbsent(id, () -> createFromFactories(id));

        return getStreamWithIdOrElseThrow(id);
    }

    private <T> Publisher<T> getStreamWithIdOrElseThrow(StreamId<T> id) {
        Publisher<T> activeStream = content.get(id);

        if (activeStream == null) {
            throw new IllegalArgumentException(
                    "The stream for id '" + id + "' is neither present nor can it be created by any factory.");
        }
        return activeStream;
    }

    private <T> void checkForRecursiveCycles(StreamId<T> id) {
        if (idsOfStreamsUnderCreation.contains(id)) {
            throw new CycleInStreamDiscoveryDetectedException(
                    format("Cycle detected when looking up streams. (At least) the following id was queried twice: %s."
                            + " Number of queried ids without revolving: %s", id, idsOfStreamsUnderCreation.size()));
        }
    }

    private void checkSameContexOfExecution() {
        if (!Thread.currentThread().equals(contextOfExecution)) {
            throw new IllegalStateException(format(
                    "Invalid context of execution. It is not allowed to recursively discover streams from different"
                            + " threads. The allowed thread is [%s] while the current one is [%s]",
                    contextOfExecution.getName(), Thread.currentThread().getName()));
        }
    }

    private <T> TrackKeepingDiscoveryService cloneDiscoveryServiceIncluding(StreamId<T> newId) {
        Set<StreamId<?>> newSet = new HashSet<>(idsOfStreamsUnderCreation);
        newSet.add(newId);
        return new TrackKeepingDiscoveryService(factories, content, newSet, contextOfExecution);
    }

    private <T> Publisher<T> createFromFactories(StreamId<T> newId) {
        for (StreamFactory factory : factories) {
            Optional<Publisher<T>> factoryResult = factory.create(newId, cloneDiscoveryServiceIncluding(newId));

            if (factoryResult == null) {
                throw new IllegalStateException(
                        format("Factory %s returned null instead of a valid Optional for the id %s", factory, newId));
            }

            if (factoryResult.isPresent()) {
                Publisher<T> stream = factoryResult.get();
                if (stream == null) {
                    throw new IllegalStateException(
                            format("Factory %s returned a null stream for the id %s", factory, newId));
                }
                LOGGER.info(format("Stream from id '%s' was successfully created by factory '%s'", newId, factory));
                return stream;
            }
        }
        return null;
    }

}
