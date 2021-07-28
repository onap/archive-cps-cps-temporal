/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021 Bell Canada.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.temporal.service;

import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.springframework.data.domain.Page;

public interface NetworkDataService {

    /**
     * Add Network data.
     *
     * @param networkData the network data to be stored
     */
    NetworkData addNetworkData(NetworkData networkData);

    Page<NetworkData> searchNetworkData(SearchCriteria searchCriteria);
}
