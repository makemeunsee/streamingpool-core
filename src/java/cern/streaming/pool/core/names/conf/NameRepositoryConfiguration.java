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

package cern.streaming.pool.core.names.conf;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.streaming.pool.core.names.ConstantsContainer;
import cern.streaming.pool.core.names.NameRepositories;
import cern.streaming.pool.core.names.NameRepository;
import cern.streaming.pool.core.names.resolve.Chains;
import cern.streaming.pool.core.names.resolve.Names;

@Configuration
public class NameRepositoryConfiguration {

    @Autowired
    private List<ConstantsContainer> expressionConstantsContainers;

    @Bean
    public NameRepository nameRepository() {
        return NameRepositories.newFromConstantContainers(expressionConstantsContainers);
    }

    @Bean
    public Function<Object, String> streamIdNameMapping(NameRepository nameRepository) {
        // @formatter:off
        return Chains
                .<String>chain().or(nameRepository::nameFor)
                .or(Names::fromNameMethod)
                .or(Names::fromGetNameMethod)
                .or(Names::fromOverriddenToString)
                .or(Names::fromSimpleClassName)
                .orElseNull();
        // @formatter:on
    }

}
