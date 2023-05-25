/*******************************************************************************
 * Copyright (c) 2021,2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.bpdm.common.service

import mu.KLogger
import org.eclipse.tractusx.bpdm.common.exception.BpdmSyncConflictException
import org.eclipse.tractusx.bpdm.common.exception.BpdmSyncStateException
import org.eclipse.tractusx.bpdm.common.model.BaseSyncRecord
import org.eclipse.tractusx.bpdm.common.model.SyncStatus
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Uses transaction isolation level "serializable" in order to make sure that in case of parallel execution on different spring boot instances,
 * only one instance can get the sync record and make changes like setting it to "running" state at the same time.
 */
abstract class BaseSyncRecordService<SYNC_TYPE : Enum<*>, SYNC_RECORD : BaseSyncRecord<SYNC_TYPE>> {
    companion object {
        val syncStartTime: Instant = LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)
    }

    protected abstract val logger: KLogger

    protected abstract fun newSyncRecord(type: SYNC_TYPE, syncStartTime: Instant): SYNC_RECORD

    protected abstract fun save(record: SYNC_RECORD): SYNC_RECORD

    protected abstract fun findByType(type: SYNC_TYPE): SYNC_RECORD?

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun getOrCreateRecord(type: SYNC_TYPE): SYNC_RECORD {
        return findByType(type) ?: run {
            logger.info { "Create new sync record entry for type $type" }
            val newEntry = newSyncRecord(type, syncStartTime)
            save(newEntry)
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun setSynchronizationStart(type: SYNC_TYPE): SYNC_RECORD {
        val record = getOrCreateRecord(type)

        if (record.status == SyncStatus.RUNNING)
            throw BpdmSyncConflictException(type)

        logger.debug { "Set sync of type ${record.type} to status ${SyncStatus.RUNNING}" }

        record.errorDetails = null

        if (record.status != SyncStatus.ERROR) {
            record.fromTime = record.startedAt ?: syncStartTime
            record.errorDetails = null
            record.errorSave = null
            record.startedAt = Instant.now().truncatedTo(ChronoUnit.MICROS)
            record.finishedAt = null
            record.count = 0
            record.progress = 0f
        }
        record.status = SyncStatus.RUNNING

        return save(record)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun setSynchronizationSuccess(type: SYNC_TYPE): SYNC_RECORD {
        val record = getOrCreateRecord(type)
        if (record.status != SyncStatus.RUNNING)
            throw BpdmSyncStateException("Synchronization of type ${record.type} can't switch from state ${record.status} to ${SyncStatus.SUCCESS}.")

        logger.debug { "Set sync of type ${record.type} to status ${SyncStatus.SUCCESS}" }

        record.finishedAt = Instant.now().truncatedTo(ChronoUnit.MICROS)
        record.progress = 1f
        record.status = SyncStatus.SUCCESS
        record.errorDetails = null
        record.errorSave = null

        return save(record)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun setSynchronizationError(type: SYNC_TYPE, errorMessage: String, saveState: String?): SYNC_RECORD {
        val record = getOrCreateRecord(type)
        logger.debug { "Set sync of type ${record.type} to status ${SyncStatus.ERROR} with message $errorMessage" }

        record.finishedAt = Instant.now().truncatedTo(ChronoUnit.MICROS)
        record.status = SyncStatus.ERROR
        record.errorDetails = errorMessage.take(255)
        record.errorSave = saveState

        return save(record)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun setProgress(type: SYNC_TYPE, count: Int, progress: Float): SYNC_RECORD {
        val record = getOrCreateRecord(type)
        if (record.status != SyncStatus.RUNNING)
            throw BpdmSyncStateException("Synchronization of type ${record.type} can't change progress when not running.")

        logger.debug { "Update progress of sync type ${record.type} to $progress" }

        record.count = count
        record.progress = progress

        return save(record)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun reset(type: SYNC_TYPE): SYNC_RECORD {
        val record = getOrCreateRecord(type)
        logger.debug { "Reset sync status of type ${record.type}" }

        record.status = SyncStatus.NOT_SYNCED
        record.errorDetails = null
        record.errorSave = null
        record.startedAt = null
        record.finishedAt = null
        record.count = 0
        record.progress = 0f
        record.fromTime = syncStartTime

        return save(record)
    }
}