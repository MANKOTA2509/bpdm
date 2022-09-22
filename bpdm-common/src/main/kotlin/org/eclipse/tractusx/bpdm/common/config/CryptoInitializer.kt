/*******************************************************************************
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.bpdm.common.config

import org.keycloak.common.crypto.CryptoIntegration
import org.keycloak.common.util.CertificateUtils
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

/**
 * This is a workaround to deal with an issue in the keycloak adapter. Prevents exception being thrown when a signing key with elliptic curve algorithm
 * ES256 is configured in the keycloak instance, which can be seen at https://<keycloak-host>/auth/realms/<realm>/protocol/openid-connect/certs.
 */
@Configuration
class CryptoInitializer {

    @EventListener(ApplicationReadyEvent::class)
    fun initCrypto() {
        CryptoIntegration.init(CertificateUtils::class.java.classLoader)
    }
}