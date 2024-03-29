/*
 * ============LICENSE_START=======================================================
 * Copyright (c) 2021 Bell Canada.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

package org.onap.cps.temporal.domain;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.io.Serializable;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 * Entity to store an anchor configuration or state along with the moment it has been observed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(NetworkDataId.class)
@Builder
@Entity
@Table(name = "network_data")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class NetworkData implements Serializable {

    private static final long serialVersionUID = 8886477871334560919L;

    @Id
    @NotNull
    @Column
    private OffsetDateTime observedTimestamp;

    @Id
    @NotNull
    @Column
    private String dataspace;

    @Id
    @NotNull
    @Column
    private String anchor;

    @NotNull
    @Column(updatable = false)
    private String schemaSet;

    @NotNull
    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    private Operation operation;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", updatable = false)
    private String payload;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdTimestamp;

}
