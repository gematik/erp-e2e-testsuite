/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.rest

import arrow.core.Either
import de.gematik.test.erezept.primsys.PrimSysRestException
import de.gematik.test.erezept.primsys.data.error.ErrorDto
import io.ktor.http.*


class PrimSysResponse<T>(val statusCode: Int, val headers: Headers, val body: Either<ErrorDto, T>) {

    /**
     * Unwrap the Either-Body and get the expected response body
     *
     * **Attention: ** if the body contains an [ErrorDto] this method will throw a [PrimSysRestException]
     */
    fun asExpectedPayload(): T {
        return this.body.getOrNull() ?: throw PrimSysRestException(statusCode, "unexpected response body")
    }

    fun asError(): ErrorDto {
        return this.body.leftOrNull() ?: throw PrimSysRestException(statusCode, "expected error in body but no error found")
    }

    fun successful(): Boolean = this.body.isRight()
}