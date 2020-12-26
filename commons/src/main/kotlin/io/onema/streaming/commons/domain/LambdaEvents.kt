/**
 * This file is part of the ONEMA streaming Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.commons.domain

import java.io.File

data class MetadataInfo(val bucket: String, val metadataKey: String, val framesKey: String, val videoName: String) {
    fun path(): String = File(metadataKey).parent
    fun videoFileName(): String = "${File(metadataKey).nameWithoutExtension}.ts"
    fun videoS3Origin(): String = "s3://${bucket}.s3.amazonaws.com/${path()}/${videoFileName()}"

    fun metadataName(): String = File(metadataKey).name
    fun metadataS3Origin(): String = "s3://${bucket}.s3.amazonaws.com/${metadataKey}"

    fun frameName(): String = File(framesKey).name
    fun frameS3Origin(): String = "s3://${bucket}.s3.amazonaws.com/${framesKey}"

}
