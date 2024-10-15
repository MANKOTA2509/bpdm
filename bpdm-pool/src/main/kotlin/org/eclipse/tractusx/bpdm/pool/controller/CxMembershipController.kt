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

package org.eclipse.tractusx.bpdm.pool.controller

import org.eclipse.tractusx.bpdm.common.dto.PageDto
import org.eclipse.tractusx.bpdm.common.dto.PaginationRequest
import org.eclipse.tractusx.bpdm.pool.api.PoolCxMembershipApi
import org.eclipse.tractusx.bpdm.pool.api.model.CxMembershipDto
import org.eclipse.tractusx.bpdm.pool.api.model.request.CxMembershipSearchRequest
import org.eclipse.tractusx.bpdm.pool.api.model.request.CxMembershipUpdateRequest
import org.eclipse.tractusx.bpdm.pool.config.PermissionConfigProperties
import org.eclipse.tractusx.bpdm.pool.service.CxMembershipService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class CxMembershipController(
    private val cxMembershipService: CxMembershipService
): PoolCxMembershipApi {

    @PreAuthorize("hasAuthority(${PermissionConfigProperties.WRITE_PARTNER})")
    override fun get(searchRequest: CxMembershipSearchRequest, paginationRequest: PaginationRequest): PageDto<CxMembershipDto> {
       return cxMembershipService.searchMemberships(searchRequest, paginationRequest)
    }

    @PreAuthorize("hasAuthority(${PermissionConfigProperties.WRITE_PARTNER})")
    override fun put(updateRequest: CxMembershipUpdateRequest) {
        return cxMembershipService.updateMemberships(updateRequest)
    }
}