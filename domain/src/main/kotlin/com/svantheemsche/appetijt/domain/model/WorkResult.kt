/*
 * Appetijt: A local-first meal planning Android application.
 * Copyright (C) 2026 Stefan Van Theemsche
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.svantheemsche.appetijt.domain.model

sealed class WorkResult<out T> {
    data class Success<out T>(val data: T) : WorkResult<T>()
    data class Failure(
        val code: String,
        val message: String,
        val cause: Throwable? = null
    ) : WorkResult<Nothing>()
}

object ErrorCodes {
    const val INVALID_URL = "INVALID_URL"
    const val NETWORK_ERROR = "NETWORK_ERROR"
    const val METADATA_NOT_FOUND = "METADATA_NOT_FOUND"
    const val DATABASE_ERROR = "DATABASE_ERROR"
    const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
}
