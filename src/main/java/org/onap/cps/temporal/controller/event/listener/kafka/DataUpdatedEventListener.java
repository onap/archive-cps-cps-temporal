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
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.temporal.controller.event.listener.kafka;

import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.MISSING;
import static org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException.InvalidField.ErrorType.UNEXPECTED;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.event.model.EventSchemaMapper;
import org.onap.cps.event.model.v1.CpsDataUpdatedEvent;
import org.onap.cps.temporal.controller.event.listener.exception.EventListenerException;
import org.onap.cps.temporal.controller.event.listener.exception.InvalidEventEnvelopException;
import org.onap.cps.temporal.controller.event.model.CpsDataUpdatedEventMapper;
import org.onap.cps.temporal.service.NetworkDataService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Listener for data updated events.
 */
@Component
@Slf4j
public class DataUpdatedEventListener {

    private static final String EVENT_SCHEMA_URN_PREFIX = "urn:cps:org.onap.cps:data-updated-event-schema:v";

    private static final URI EVENT_SOURCE;

    static {
        try {
            EVENT_SOURCE = new URI("urn:cps:org.onap.cps");
        } catch (final URISyntaxException e) {
            throw new EventListenerException("Invalid URI for event source.", e);
        }
    }

    private static final String EVENT_TYPE = "org.onap.cps.data-updated-event";

    private final NetworkDataService networkDataService;
    private final CpsDataUpdatedEventMapper cpsDataUpdatedEventMapper;
    private final EventSchemaMapper eventSchemaMapper;

    /**
     * Constructor.
     */
    public DataUpdatedEventListener(
            final NetworkDataService networkDataService,
            final CpsDataUpdatedEventMapper cpsDataUpdatedEventMapper,
            final EventSchemaMapper eventSchemaMapper) {
        this.networkDataService = networkDataService;
        this.cpsDataUpdatedEventMapper = cpsDataUpdatedEventMapper;
        this.eventSchemaMapper = eventSchemaMapper;
    }

    /**
     * Consume the specified event from v0 event schema.
     *
     * @param cpsDataUpdatedEvent the data updated event to be consumed and persisted.
     */
    @KafkaListener(
        topics = "${app.listener.data-updated.v0.topic}",
        errorHandler = "dataUpdatedEventListenerErrorHandler",
        autoStartup = "${app.listener.data-updated.v0.autoStartup}")
    public void consume(final org.onap.cps.event.model.v0.CpsDataUpdatedEvent cpsDataUpdatedEvent) {
        log.debug("Consuming {} ...", cpsDataUpdatedEvent);
        final CpsDataUpdatedEvent eventV1 = this.eventSchemaMapper.v0ToV1(cpsDataUpdatedEvent);
        consume(eventV1);
    }

    /**
     * Consume the specified event from v1 event schema.
     *
     * @param cpsDataUpdatedEvent the data updated event to be consumed and persisted.
     */
    @KafkaListener(
        topics = "${app.listener.data-updated.v1.topic}",
        errorHandler = "dataUpdatedEventListenerErrorHandler",
        autoStartup = "${app.listener.data-updated.v1.autoStartup}")
    public void consume(final CpsDataUpdatedEvent cpsDataUpdatedEvent) {

        log.debug("Consuming {} ...", cpsDataUpdatedEvent);

        // Validate event envelop
        validateEventEnvelop(cpsDataUpdatedEvent);

        // Validate content operation
        validateContentOperation(cpsDataUpdatedEvent);

        // Map event to entity
        final var networkData = this.cpsDataUpdatedEventMapper.eventToEntity(cpsDataUpdatedEvent);
        log.debug("Persisting {} ...", networkData);

        // Persist entity
        final var persistedNetworkData = this.networkDataService.addNetworkData(networkData);
        log.debug("Persisted {}", persistedNetworkData);

    }

    private void validateEventEnvelop(final CpsDataUpdatedEvent cpsDataUpdatedEvent) {

        final var invalidEventEnvelopException =
                new InvalidEventEnvelopException("Validation failure", cpsDataUpdatedEvent);

        // Validate schema
        if (cpsDataUpdatedEvent.getSchema() == null
                || !cpsDataUpdatedEvent.getSchema().toString().startsWith(EVENT_SCHEMA_URN_PREFIX)) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "schema",
                            cpsDataUpdatedEvent.getSchema() != null ? cpsDataUpdatedEvent.getSchema().toString() : null,
                            EVENT_SCHEMA_URN_PREFIX + "*"));
        }
        // Validate id
        if (!StringUtils.hasText(cpsDataUpdatedEvent.getId())) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            MISSING, "id", null, null));
        }
        // Validate source
        if (!EVENT_SOURCE.equals(cpsDataUpdatedEvent.getSource())) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "source",
                            cpsDataUpdatedEvent.getSource() != null ? cpsDataUpdatedEvent.getSource().toString() : null,
                            EVENT_SOURCE.toString()));
        }
        // Validate type
        if (!EVENT_TYPE.equals(cpsDataUpdatedEvent.getType())) {
            invalidEventEnvelopException.addInvalidField(
                    new InvalidEventEnvelopException.InvalidField(
                            UNEXPECTED, "type", cpsDataUpdatedEvent.getType(), EVENT_TYPE));
        }

        if (invalidEventEnvelopException.hasInvalidFields()) {
            throw invalidEventEnvelopException;
        }

    }

    private void validateContentOperation(final CpsDataUpdatedEvent cpsDataUpdatedEvent) {
        if (cpsDataUpdatedEvent.getContent() == null) {
            throw new IllegalArgumentException("Event content is missing");
        }
        if (cpsDataUpdatedEvent.getContent().getOperation() == null) {
            throw new IllegalArgumentException("Event content operation is missing");
        }
    }

}
