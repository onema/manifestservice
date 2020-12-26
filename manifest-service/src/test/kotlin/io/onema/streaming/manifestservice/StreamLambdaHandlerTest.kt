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

package io.onema.streaming.manifestservice

import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.onema.playlist.hls.master
import io.onema.streaming.commons.domain.Segment
import io.onema.streaming.commons.domain.StreamData
import io.onema.streaming.commons.extensions.nameWithoutExtension
import io.onema.streaming.commons.extensions.renditionMetadata
import io.onema.streaming.commons.extensions.renditionSegments
import org.apache.commons.vfs2.FileObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.io.ByteArrayOutputStream

class StreamLambdaHandlerTest {

    val mapper = jacksonObjectMapper()

    @Test
    fun `Assert streamRequest responds with master manifest`() {
        // Arrange
        val masterRequest = """{"resource":"/video/{videoName}/master.m3u8","path":"/video/bbb/master.m3u8","httpMethod":"GET","headers":{"Accept":"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8","Accept-Encoding":"gzip, deflate, br","Accept-Language":"en-us","CloudFront-Forwarded-Proto":"https","CloudFront-Is-Desktop-Viewer":"false","CloudFront-Is-Mobile-Viewer":"true","CloudFront-Is-SmartTV-Viewer":"false","CloudFront-Is-Tablet-Viewer":"false","CloudFront-Viewer-Country":"US","Host":"manifest.awssd.org","User-Agent":"Mozilla/5.0 (iPhone; CPU iPhone OS 14_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.1 Mobile/15E148 Safari/604.1","Via":"2.0 f815d676e23e62be6eba5756491a262d.cloudfront.net (CloudFront)","X-Amz-Cf-Id":"7nAfzWLrkB4fM0mLqZ5lvc_ESJ7hP1uVTaSQl6K8frwR57JqxjC52A==","X-Amzn-Trace-Id":"Root=1-5fe67905-0d38d4bb29a887e048fb47ca","X-Forwarded-For":"172.58.16.188, 130.176.4.147","X-Forwarded-Port":"443","X-Forwarded-Proto":"https"},"multiValueHeaders":{"Accept":["text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"],"Accept-Encoding":["gzip, deflate, br"],"Accept-Language":["en-us"],"CloudFront-Forwarded-Proto":["https"],"CloudFront-Is-Desktop-Viewer":["false"],"CloudFront-Is-Mobile-Viewer":["true"],"CloudFront-Is-SmartTV-Viewer":["false"],"CloudFront-Is-Tablet-Viewer":["false"],"CloudFront-Viewer-Country":["US"],"Host":["manifest.awssd.org"],"User-Agent":["Mozilla/5.0 (iPhone; CPU iPhone OS 14_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.1 Mobile/15E148 Safari/604.1"],"Via":["2.0 f815d676e23e62be6eba5756491a262d.cloudfront.net (CloudFront)"],"X-Amz-Cf-Id":["7nAfzWLrkB4fM0mLqZ5lvc_ESJ7hP1uVTaSQl6K8frwR57JqxjC52A=="],"X-Amzn-Trace-Id":["Root=1-5fe67905-0d38d4bb29a887e048fb47ca"],"X-Forwarded-For":["172.58.16.188, 130.176.4.147"],"X-Forwarded-Port":["443"],"X-Forwarded-Proto":["https"]},"queryStringParameters":null,"multiValueQueryStringParameters":null,"pathParameters":{"videoName":"bbb"},"stageVariables":null,"requestContext":{"resourceId":"gaxivn","resourcePath":"/video/{videoName}/master.m3u8","httpMethod":"GET","extendedRequestId":"YIfY1EoloAMFU-A=","requestTime":"25/Dec/2020:23:43:01 +0000","path":"/video/bbb/master.m3u8","accountId":"065150860170","protocol":"HTTP/1.1","stage":"dev","domainPrefix":"manifest","requestTimeEpoch":1608939781217,"requestId":"4257dcc9-f9cf-4a0e-8ce0-9214ab300936","identity":{"cognitoIdentityPoolId":null,"accountId":null,"cognitoIdentityId":null,"caller":null,"sourceIp":"172.58.16.188","principalOrgId":null,"accessKey":null,"cognitoAuthenticationType":null,"cognitoAuthenticationProvider":null,"userArn":null,"userAgent":"Mozilla/5.0 (iPhone; CPU iPhone OS 14_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.1 Mobile/15E148 Safari/604.1","user":null},"domainName":"manifest.awssd.org","apiId":"m7k3wcvard"},"body":null,"isBase64Encoded":false}"""
        val requestStream = masterRequest.byteInputStream()
        val responseStream = ByteArrayOutputStream()
        val handler = StreamLambdaHandler()
        val expectedManifest = master playlist {
            version set 5
            stream {
                streamInf resolution "320x180" codecs "avc1.4d0015,mp4a.40.2" bandwidth 188049 frameRate 60F
                info name "foo" rendition "320x180"
            }
        }

        // Act
        handler.handleRequest(requestStream, responseStream, null)
        val response = mapper.readValue<AwsProxyResponse>(responseStream.toString())

        // Assert
        assertEquals(200, response.statusCode)
        assertEquals(expectedManifest, response.body)

    }

    @Test
    fun `Assert streamRequest responds with media data`() {
        // Arrange
        val mediaRequest = """{"resource":"/media/{mediaName+}","path":"/media/320x180","httpMethod":"GET","headers":{"accept":"*/*","accept-encoding":"gzip","accept-language":"en-us","Host":"manifest.awssd.org","referer":"https://manifest.awssd.org/master","User-Agent":"AppleCoreMedia/1.0.0.19G2021 (Macintosh; U; Intel Mac OS X 10_15_6; en_us)","X-Amzn-Trace-Id":"Root=1-5f5ebf0a-5","X-Forwarded-For":"1.2.23.101","X-Forwarded-Port":"443","X-Forwarded-Proto":"https","x-playback-session-id":"75787E01-2"},"multiValueHeaders":{"accept":["*/*"],"accept-encoding":["gzip"],"accept-language":["en-us"],"Host":["manifest.awssd.org"],"referer":["https://manifest.awssd.org/master"],"User-Agent":["AppleCoreMedia/1.0.0.19G2021 (Macintosh; U; Intel Mac OS X 10_15_6; en_us)"],"X-Amzn-Trace-Id":["Root=1-5f5ebf0a-56646f001a33fb0074360f80"],"X-Forwarded-For":["1.2.23.101"],"X-Forwarded-Port":["443"],"X-Forwarded-Proto":["https"],"x-playback-session-id":["75787E01-2"]},"queryStringParameters":null,"multiValueQueryStringParameters":null,"pathParameters":{"mediaName+":"320x180"},"stageVariables":null,"requestContext":{"resourceId":"aj2fet","resourcePath":"/media/{mediaName+}","httpMethod":"GET","extendedRequestId":"S1LJmFrXIAMFcbQ=","requestTime":"14/Sep/2020:00:53:30 +0000","path":"/media/320x180","accountId":"123456789012","protocol":"HTTP/1.1","stage":"dev","domainPrefix":"manifest","requestTimeEpoch":1600044810122,"requestId":"d572014a-b","identity":{"cognitoIdentityPoolId":null,"accountId":null,"cognitoIdentityId":null,"caller":null,"sourceIp":"1.2.23.101","principalOrgId":null,"accessKey":null,"cognitoAuthenticationType":null,"cognitoAuthenticationProvider":null,"userArn":null,"userAgent":"AppleCoreMedia/1.0.0.19G2021 (Macintosh; U; Intel Mac OS X 10_15_6; en_us)","user":null},"domainName":"manifest.awssd.org","apiId":"xfxoeex5e4"},"body":null,"isBase64Encoded":false}"""
        val requestStream = mediaRequest.byteInputStream()
        val responseStream = ByteArrayOutputStream()
        val handler = StreamLambdaHandler()

        // Act
        handler.handleRequest(requestStream, responseStream, null)
        val response = mapper.readValue<AwsProxyResponse>(responseStream.toString())

        // Assert
        assertEquals(200, response.statusCode)

    }
}