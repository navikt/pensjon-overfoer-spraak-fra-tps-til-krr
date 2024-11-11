package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread

import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Component

@Component
class ClientCredentialsSupportFactory(private val azureAdClientCredentialsTokenService: AzureAdClientCredentialsTokenService) {
    fun createTokenInterceptor(scopes: List<String>): ClientHttpRequestInterceptor = ClientCredentialsClientHttpRequestInterceptor(createClientCredentialsTokenService(verifyScopes(scopes)))

    fun createClientCredentialsTokenService(scopes: List<String>): ClientCredentialsTokenService = ClientCredentialsTokenService(
        azureAdClientCredentialsTokenService,
        verifyScopes(scopes)
    )

    private fun verifyScopes(scopes: List<String>): List<String> {
        require(scopes.all { it.isNotBlank() }) { "All scope values must be non null and non empty got [${scopes.joinToString()}]" }
        return scopes
    }
}
