package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread;

interface AzureAdClientCredentialsTokenService {
    fun fetch(scope: List<String>): ClientCredentialsTokenResponse
}
