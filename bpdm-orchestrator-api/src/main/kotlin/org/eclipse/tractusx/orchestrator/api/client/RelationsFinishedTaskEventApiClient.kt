/*******************************************************************************
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.eclipse.tractusx.orchestrator.api.client

import org.eclipse.tractusx.bpdm.common.dto.PaginationRequest
import org.eclipse.tractusx.orchestrator.api.ApiCommons
import org.eclipse.tractusx.orchestrator.api.RelationsFinishedTaskEventApi
import org.eclipse.tractusx.orchestrator.api.model.FinishedTaskEventsResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import java.time.Instant

@HttpExchange
interface RelationsFinishedTaskEventApiClient : RelationsFinishedTaskEventApi{

    @GetExchange(value = "${ApiCommons.BASE_PATH_V7_RELATIONS}/finished-events")
    override fun getRelationsEvents(@RequestParam timestamp: Instant, @ParameterObject paginationRequest: PaginationRequest): FinishedTaskEventsResponse
}