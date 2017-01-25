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

package cern.streaming.pool.core.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Utility methods for collections
 */
public final class MoreCollections {

    private MoreCollections() {
        /* only static methods */
    }

    public static <T> List<T> emptyIfNull(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public static <T> Set<T> emptyIfNull(Set<T> set) {
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

}
