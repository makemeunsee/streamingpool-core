// @formatter:off
/*
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

package org.streamingpool.core.conf;

import static org.streamingpool.core.util.MoreCollections.emptyIfNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.streamingpool.core.service.StreamFactory;
import org.streamingpool.core.service.TypedStreamFactory;
import org.streamingpool.core.service.impl.LocalPool;

/**
 * The spring configuration which shall be used in any application that will have the spring pool embedded. It provides
 * a pool which will pick up the following beans automatically:
 * <p>
 * Dependency injection:
 * <ul>
 * <li>{@link TypedStreamFactory}: Any additional Stream factory will be automatically plugged into the pool to be used for
 * stream discovery.
 * </ul>
 *
 * @author kfuchsbe
 */
@Configuration
@Import({TestSchedulerConfiguration.class, DefaultSchedulerConfiguration.class})
public class EmbeddedPoolConfiguration {

    /**
     * A list of stream factories which will be automatically collected by Spring. Since there will be at least one (the
     * below created factory) we can keep the required=true (default).
     */
    @Autowired(required = false)
    private List<StreamFactory> streamFactories;

    @Autowired
    private Scheduler scheduler;

    @Bean
    public LocalPool pool() {
        LocalPool localPool = new LocalPool(emptyIfNull(streamFactories), scheduler);
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        String style = Stream.of(
                "shape: box;",
                "size: 5px, 5px;",
                "fill-mode: plain;",
                "text-alignment: right;",
                "text-padding: 5px;",
                "text-offset: 6px, 0px;",
                "fill-color: red;",
                "stroke-mode: plain;",
                "stroke-color: blue;").collect(Collectors.joining());

        LocalPool.graph.addAttribute("ui.quality");
        LocalPool.graph.addAttribute("ui.antialias");
        
        LocalPool.graph.setAttribute("ui.stylesheet", "node {" + style + "}");
        LocalPool.graph.display();
        return localPool;
    }

}
