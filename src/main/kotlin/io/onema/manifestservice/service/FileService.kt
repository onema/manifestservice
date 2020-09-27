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

package io.onema.manifestservice.service

import org.apache.commons.vfs2.FileObject
import java.io.InputStream

interface FileService {
    fun readBytes(file: FileObject, start: Long, end: Long): ByteArray
}