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

import java.util.Optional;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.NetworkDataId;
import org.onap.cps.temporal.domain.Operation;
import org.onap.cps.temporal.domain.SearchCriteria;
import org.onap.cps.temporal.repository.NetworkDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Network Data.
 */
@Service
@Slf4j
public class NetworkDataServiceImpl implements NetworkDataService {

    private final NetworkDataRepository networkDataRepository;
    private final int maxPageSize;

    public NetworkDataServiceImpl(final NetworkDataRepository networkDataRepository,
                                  final @Value("${app.query.response.max-page-size}") int maxPageSize) {
        this.networkDataRepository = networkDataRepository;
        this.maxPageSize = maxPageSize;
    }

    @Override
    public NetworkData addNetworkData(final NetworkData networkData) {
        validateNetworkData(networkData);
        final var savedNetworkData = networkDataRepository.save(networkData);
        if (savedNetworkData.getCreatedTimestamp() == null) {
            // Data already exists and can not be inserted
            final var id =
                    new NetworkDataId(
                            networkData.getObservedTimestamp(), networkData.getDataspace(), networkData.getAnchor());
            final Optional<NetworkData> existingNetworkData = networkDataRepository.findById(id);
            throw new ServiceException(
                    "Failed to create network data. It already exists: " + (existingNetworkData.orElse(null)));
        }
        return savedNetworkData;
    }

    private void validateNetworkData(final NetworkData networkData) {
        if (networkData.getOperation() != Operation.DELETE
                && networkData.getPayload() == null) {
            throw new ValidationException(
                    String.format("The operation %s must not have null payload", networkData.getOperation()));
        }
    }

    @Override
    public Slice<NetworkData> searchNetworkData(final SearchCriteria searchCriteria) {
        if (searchCriteria.getPageable().getPageSize() > maxPageSize) {
            throw new ValidationException("page-size must be less than or equals to " + maxPageSize);
        }
        return networkDataRepository.findBySearchCriteria(searchCriteria);
    }

}
