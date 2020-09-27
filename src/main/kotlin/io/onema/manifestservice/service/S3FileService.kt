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

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.github.vfss3.S3FileObject
import org.apache.commons.vfs2.FileObject

class S3FileService(val s3: AmazonS3) : FileService {
    override fun readBytes(file: FileObject, start: Long, end: Long): ByteArray {
        file as S3FileObject
        val bucket = file.name.bucket
        val key = file.name.path.trim('/')
        val request = GetObjectRequest(bucket, key)
        request.setRange(start, end)
        return s3.getObject(request).objectContent.readAllBytes()
    }
}