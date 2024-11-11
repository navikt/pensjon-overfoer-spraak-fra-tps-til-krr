package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations

@Component
class DigdirKrrProxyClient(
    @Value("\${KRR_ENDPOINT}")    val endpoint: String,
    val restOperations: RestOperations
) {
    private val logger: Logger = LoggerFactory.getLogger(DigdirKrrProxyClient::class.java)

    fun setSpraakForAnalogBruker(pid: String, spraak: String): Boolean {
        val headers = HttpHeaders()
            .apply {
                set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
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
}