/**
 * This file is part of the ONEMA manifestservice Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.manifestservice.extensions

import java.util.*

fun Int?.hex(): String = Integer.toHexString(this ?: 0)

fun Int.pad(): String = this.toString().padStart(12, '0')

fun String.splitRange(): List<Long> = this
    .split('=')
    .last()
    .split('-')
    .map { it.toLong() }

fun ByteArray.base64Encode(): String {
    return Base64.getEncoder().encodeToString(this)
}