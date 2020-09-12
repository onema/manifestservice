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

import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.net.URI

@Configuration
class OriginConfig {

    @Autowired
    lateinit var resourceLoader: ResourceLoader

    @Value("\${ORIGIN}")
    lateinit var origin: URI


    @Bean
    fun originFiles(): List<FileObject> {
        val fsManager: FileSystemManager = VFS.getManager()
        val dir: FileObject = fsManager.resolveFile(origin)
        return dir.children.toList()
    }
}
