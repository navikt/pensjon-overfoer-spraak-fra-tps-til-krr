package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class ClientCredentialsClientHttpRequestInterceptor(private val clientCredentialsTokenService: ClientCredentialsTokenService) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + clientCredentialsTokenService.accessToken())
        return execution.execute(request, body)
    }

}
