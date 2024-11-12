package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations
import java.util.UUID

@Component
class DigdirKrrProxyClient(
    @Value("\${KRR_ENDPOINT}") val endpoint: String,
    val restOperations: RestOperations
) {
    private val logger: Logger = LoggerFactory.getLogger(DigdirKrrProxyClient::class.java)


    fun hentSpraak(pid: String): String? {

        val headers = HttpHeaders()
            .apply {
                set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                set("Nav-Call-Id", UUID.randomUUID().toString())
                set("Nav-Personident", pid)
            }

        try {
            return restOperations.exchange(
                "$endpoint/rest/v1/person",
                HttpMethod.GET,
                HttpEntity(null, headers),
                Kontaktinfo::class.java
            ).body?.spraak
        } catch (e: RestClientException) {
            if ((e as? HttpClientErrorException)?.statusCode == NOT_FOUND) {
                logger.info("Kontaktinfo Not Found")
                return null
            } else {
                if ((e as? HttpClientErrorException)?.statusCode == BAD_REQUEST) {
                    throw RuntimeException(e)
                }
            }
            logger.warn("Kunne ikke hente kontaktinformasjon fra KRR", e)
            return null
        }
    }

    fun setSpraakForAnalogBruker(pid: String, spraak: String): Boolean {
        val headers = HttpHeaders()
            .apply {
                set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                set("Nav-Call-Id", UUID.randomUUID().toString())
                set("Nav-Personident", pid)
            }
        try {
            restOperations.exchange(
                "$endpoint/rest/v1/person/spraak",
                HttpMethod.PUT,
                HttpEntity(SetSpraakRequest(spraak), headers),
                Void::class.java
            )
        } catch (e: RestClientException) {
            logger.warn("Kan ikke sette språk i krr", e)
            return false
        }
        return true
    }

    private data class SetSpraakRequest(val spraak: String)

    private data class Kontaktinfo(
        val personident: String,
        val spraak: String?
    )
}
