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

package io.onema.streaming.commons.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.onema.streaming.commons.extensions.metadataMap
import io.onema.streaming.commons.extensions.renditionSegments
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class FrameDataTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `Assert build segments with valid data`() {

        // Arrange
        val fsManager: FileSystemManager = VFS.getManager()
        val streamDataFile = fsManager.resolveFile("res://origin/320x180.json")
        val renditionSegmentsFile = listOf(fsManager.resolveFile("res://origin/320x180_frames.json"))
        val streamData = listOf(streamDataFile.metadataMap<StreamData>()).toMap()

        // Act
        val result = renditionSegmentsFile.renditionSegments(streamData)["320x180"] ?: error("Unable to find the rendition segments")

        // Assert
        assertEquals(60F, result[0].frameRate)
        assertEquals(120, result[0].frames)
        assertEquals(0, result[0].position)
        assertEquals(45684, result[0].length)

        assertEquals(60F, result[1].frameRate)
        assertEquals(120, result[1].frames)
        assertEquals(45684, result[1].position)
        assertEquals(43052, result[1].length)

        assertEquals(60F, result[2].frameRate)
        assertEquals(60, result[2].frames)
        assertEquals(88736, result[2].position)
        assertEquals(28952, result[2].length)
    }
}
