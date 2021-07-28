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
package org.onap.cps.temporal.domain

import org.springframework.data.domain.Sort
import spock.lang.Specification

import java.time.OffsetDateTime

class SearchCriteriaSpec extends Specification {

    def myDataspace = 'my-dataspace'
    def myAnchorNames = ['my-anchor'] as Set
    def myschemaSetName = 'my-schemaset'


    def 'Search Criteria has default values if not provided.'() {

        when: 'search criteria is created'
            def searchCriteria = SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .schemaSetName(myschemaSetName)
                .pagination(0, 10)
                .build()

        then: 'search criteria has default value for sort'
            searchCriteria.getPageable().getSort() == Sort.by(Sort.Direction.DESC, 'observed_timestamp')
        and: 'pointInTime has almost current time as default value'
            OffsetDateTime.now().minusMinutes(5).isBefore(searchCriteria.getCreatedTimestampBefore())
        and: 'contains the provided value to builder'
            searchCriteria.getDataspaceName() == myDataspace
            searchCriteria.getSchemaSetName() == myschemaSetName
            searchCriteria.getPageable().getPageNumber() == 0
            searchCriteria.getPageable().getPageSize() == 10

    }

    def 'Search Criteria with the provided values.'() {

        given: 'sort by parameter'
            def sortBy = Sort.by(Sort.Direction.ASC, 'observed_timestamp')
        and: 'point in time is one day ago'
            def lastDayAsPointInTime = OffsetDateTime.now().minusDays(1)
        and: 'observed timestamp'
            def nowAsObservedTimestamp= OffsetDateTime.now()

        when: 'search criteria is created'
            def searchCriteria = SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .schemaSetName(myschemaSetName)
                .anchorNames(myAnchorNames)
                .pagination(0, 10)
                .sort(sortBy)
                .observedTimestampAfter(nowAsObservedTimestamp)
                .createdTimestampBefore(lastDayAsPointInTime)
                .build()

        then: 'search criteria has expected value'
            with(searchCriteria) {
                dataspaceName == myDataspace
                schemaSetName == schemaSetName
                anchorNames == myAnchorNames
                observedTimestampAfter == nowAsObservedTimestamp
                createdTimestampBefore == lastDayAsPointInTime
                pageable.getPageNumber() == 0
                pageable.getPageSize() == 10
                pageable.getSort() == sortBy
            }

    }

    def 'Error handling: missing dataspace.'() {
        when: 'search criteria is created without dataspace'
            SearchCriteria.builder()
                .anchorNames(myAnchorNames as Set)
                .pagination(0, 10)
                .build()
        then: 'exception is thrown'
            thrown(IllegalStateException)
    }

    def 'Error handling: missing both schemaset and anchors.'() {
        when: 'search criteria is created without schemaset and anchors'
            SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .pagination(0, 10)
                .build()
        then: 'exception is thrown'
            thrown(IllegalStateException)
    }

    def 'Error handling: missing pagination.'() {
        when: 'search criteria is created without pagination'
            SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .anchorNames(myAnchorNames)
                .build()
        then: 'exception is thrown'
            thrown(IllegalStateException)
    }

}
