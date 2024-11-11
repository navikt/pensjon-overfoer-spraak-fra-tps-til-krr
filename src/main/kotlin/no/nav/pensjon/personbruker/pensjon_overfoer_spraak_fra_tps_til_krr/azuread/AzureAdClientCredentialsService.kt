package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread

import net.logstash.logback.marker.RawJsonAppendingMarker
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

class AzureAdClientCredentialsService(
    private val clientId: String,
    private val clientSecret: String,
    private val endpoint: String, // "/oauth2/v2.0/token"
    private val restTemplate: RestTemplate
): AzureAdClientCredentialsTokenService {
    private val logger: Logger = getLogger(AzureAdClientCredentialsService::class.java)

    override fun fetch(scope: List<String>): ClientCredentialsTokenResponse = try {
        restTemplate.exchange<ClientCredentialsTokenResponse>(
            endpoint,
            POST,
            HttpEntity<MultiValueMap<String, String>>(
                LinkedMultiValueMap<String, String>().apply {
                    add("grant_type", "client_credentials")
                    add("client_id", clientId)
                    add("client_secret", clientSecret)
                    add("scope", scope.joinToString(" "))
                },
                HttpHeaders().apply {
                    contentType = APPLICATION_FORM_URLENCODED
                }
            ),
        ).body
    } catch (e: HttpClientErrorException) {
        if (e.statusCode == NOT_FOUND) {
            logger.error(
                RawJsonAppendingMarker("error_response", e.responseBodyAsString),
                "Got 404 when trying to fetch token using endpoint $endpoint"
            )
            throw ClientCredentialsException("Unable to fetch token, wrong URL", e)
        } else {
            logger.error(
                RawJsonAppendingMarker("error_response", e.responseBodyAsString),
                "Failed to fetch token for scope=${scope.joinToString(" ")}, got status=${e.statusText}, message=${e.message}"
            )
            throw ClientCredentialsException("Unable to fetch token", e)
        }
    } catch (e: HttpServerErrorException) {
        logger.error(
            RawJsonAppendingMarker("error_response", e.responseBodyAsString),
            "Failed to fetch token, got status=${e.statusText}, message=${e.message}"
        )
        throw ClientCredentialsException("Unable to fetch token", e)
    } ?: throw ClientCredentialsException("Received empty body in response")
}
