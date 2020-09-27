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

package io.onema.manifestservice.config

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.onema.manifestservice.service.FileService
import io.onema.manifestservice.service.LocalFileService
import io.onema.manifestservice.service.S3FileService
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.RuntimeException
import java.net.URI

@Configuration
class OriginConfig {

    @Value("\${ORIGIN}")
    lateinit var origin: URI


    @Bean
    fun originFiles(): List<FileObject> {
        val fsManager: FileSystemManager = VFS.getManager()
        val dir: FileObject = fsManager.resolveFile(origin)
        return dir.children.toList()
    }

    @Bean
    fun fileService(): FileService {
        return when(origin.scheme) {
            "s3" -> S3FileService(AmazonS3ClientBuilder.defaultClient())
            "file" -> LocalFileService()
            else -> throw RuntimeException("Scheme ${origin.scheme} is not supported")
        }
    }
}
