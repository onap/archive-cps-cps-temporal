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

package org.onap.cps.temporal.controller.event.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.onap.cps.event.model.Content;
import org.onap.cps.event.model.CpsDataUpdatedEvent;
import org.onap.cps.event.model.Data;
import org.onap.cps.temporal.controller.utils.DateTimeUtility;
import org.onap.cps.temporal.domain.NetworkData;
import org.onap.cps.temporal.domain.Operation;

/**
 * Mapper for data updated event schema.
 */
@Mapper(componentModel = "spring")
public abstract class CpsDataUpdatedEventMapper {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(source = "content.observedTimestamp", target = "observedTimestamp")
    @Mapping(source = "content.dataspaceName", target = "dataspace")
    @Mapping(source = "content.schemaSetName", target = "schemaSet")
    @Mapping(source = "content.anchorName", target = "anchor")
    @Mapping(source = "content.data", target = "payload")
    @Mapping(source = "content.operation", target = "operation")
    @Mapping(expression = "java(null)", target = "createdTimestamp")
    public abstract NetworkData eventToEntity(CpsDataUpdatedEvent cpsDataUpdatedEvent);

    String map(final Data data) throws JsonProcessingException {
        return data != null ? objectMapper.writeValueAsString(data) : null;
    }

    Operation map(final Content.Operation inputOperation) {
        return inputOperation == null ? Operation.UPDATE : Operation.valueOf(inputOperation.toString());
    }

    OffsetDateTime map(final String timestamp) {
        return DateTimeUtility.toOffsetDateTime(timestamp);
    }

}
