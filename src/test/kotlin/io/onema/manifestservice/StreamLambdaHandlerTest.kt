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

package io.onema.manifestservice

import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.onema.manifestservice.playlist.playlist
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class StreamLambdaHandlerTest {

    val mapper = jacksonObjectMapper()

    @Test
    fun `Assert streamRequest responds with master manifest`() {
        // Arrange
        val masterRequest = """{"resource":"/master","path":"/master.m3u8","httpMethod":"GET","headers":{"accept":"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8","accept-encoding":"gzip, deflate, br","accept-language":"en-us","Host":"manifest.awssd.org","User-Agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Safari/605.1.15","X-Amzn-Trace-Id":"Root=1-5f5eaaea-73defad5c48211584e113efb","X-Forwarded-For":"23.241.230.109","X-Forwarded-Port":"443","X-Forwarded-Proto":"https"},"multiValueHeaders":{"accept":["text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"],"accept-encoding":["gzip, deflate, br"],"accept-language":["en-us"],"Host":["manifest.awssd.org"],"User-Agent":["Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Safari/605.1.15"],"X-Amzn-Trace-Id":["Root=1-5f5eaaea-73defad5c48211584e113efb"],"X-Forwarded-For":["23.241.230.109"],"X-Forwarded-Port":["443"],"X-Forwarded-Proto":["https"]},"queryStringParameters":null,"multiValueQueryStringParameters":null,"pathParameters":null,"stageVariables":null,"requestContext":{"resourceId":"29htyx","resourcePath":"/master.m3u8","httpMethod":"GET","extendedRequestId":"S0-koFCUIAMFflw=","requestTime":"13/Sep/2020:23:27:38 +0000","path":"/master","accountId":"065150860170","protocol":"HTTP/1.1","stage":"dev","domainPrefix":"manifest","requestTimeEpoch":1600039658398,"requestId":"ab30792d-3d30-4d70-a487-1282488db5da","identity":{"cognitoIdentityPoolId":null,"accountId":null,"cognitoIdentityId":null,"caller":null,"sourceIp":"23.241.230.109","principalOrgId":null,"accessKey":null,"cognitoAuthenticationType":null,"cognitoAuthenticationProvider":null,"userArn":null,"userAgent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Safari/605.1.15","user":null},"domainName":"manifest.awssd.org","apiId":"0uyj370f38"},"body":null,"isBase64Encoded":false}"""
        val requestStream = masterRequest.byteInputStream()
        val responseStream = ByteArrayOutputStream()
        val handler = StreamLambdaHandler()
        val expectedManifest = playlist {
            STREAM_INF(resolution = "320x180", codecs = "avc1.4d0015,mp4a.40.2", bandwidth = "188049", frameRate = 60F)
            media("320x180")
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