package com.crossover.wspro.pack

import com.crossover.util.ArgUtil
import com.crossover.util.Utils
import groovy.util.logging.Log
import net.sourceforge.argparse4j.inf.Namespace
import org.springframework.stereotype.Component

import java.nio.charset.Charset

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate


@Component
@Log
class XOService {

    private static def token

    private final static def BASE_URL = 'https://api.crossover.com/api/'

    static def getToken(){
        token
    }

    protected RestTemplate restTemplate = new RestTemplate()

    private static def getCompleteUrl(url) {
        BASE_URL + url
    }

    def getEntity(clazz, url, Namespace namespace) {
        try {
            println(getCompleteUrl(url))
            restTemplate.exchange(getCompleteUrl(url), HttpMethod.GET, new HttpEntity(getHeaders(namespace)), clazz).body
        } catch (RestClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    def postEntity(clazz, body, url, Namespace namespace) {
        return restTemplate.exchange(getCompleteUrl(url), HttpMethod.POST, new HttpEntity<Object>(body, getHeaders(namespace)), clazz).body
    }

    def getHeaders(Namespace namespace) {
        Properties prop = Utils.getProperties(namespace.get(ArgUtil.CREDENTIAL));
        def user = prop.getProperty(ArgUtil.CROSS_USERNAME)
        def pass = prop.getProperty(ArgUtil.CROSS_PASSWORD)
        (token = getToken()) ? generateHeaderWithToken(token): generateHeaderWithoutToken(user, pass)
    }

    private HttpHeaders generateHeaderWithoutToken(user, pass) {
        HttpHeaders headers = new HttpHeaders()
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(Charset.forName("US-ASCII"))))
        return headers
    }

    private HttpHeaders generateHeaderWithToken(token) {
        HttpHeaders headers = new HttpHeaders()
        headers.set("X-Auth-Token", token)
        return headers
    }
}
