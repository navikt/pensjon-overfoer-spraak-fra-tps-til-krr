package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread

class ClientCredentialsTokenService(
    private val azureAdClientCredentialsTokenService: AzureAdClientCredentialsTokenService,
    private val scopes: List<String>
) {
    fun accessToken(): String = azureAdClientCredentialsTokenService.fetch(scopes).accessToken
}
