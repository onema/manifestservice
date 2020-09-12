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

fun Int?.hex(): String = Integer.toHexString(this ?: 0)

fun String.splitRange(): List<Int> = this
    .split('=')
    .last()
    .split('-')
    .map { it.toInt() }