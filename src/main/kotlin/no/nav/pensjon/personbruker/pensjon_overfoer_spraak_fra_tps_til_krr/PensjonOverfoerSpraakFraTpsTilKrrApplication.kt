package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr

import no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread.AzureAdClientCredentialsService
import no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread.CachingAzureAdClientCredentialsTokenService
import no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread.ClientCredentialsSupportFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableScheduling
class PensjonOverfoerSpraakFraTpsTilKrrApplication {
	@Bean
	fun restTemplateDigdirKrrProxy(
		clientCredentialsSupportFactory: ClientCredentialsSupportFactory,
		@Value("\${KRR_SCOPE}") scope: String
	): RestTemplate = RestTemplate().apply {
		interceptors = listOf(
			clientCredentialsSupportFactory.createTokenInterceptor(listOf(scope)),
		)
	}

	@Bean
	fun azureAdClientCredentialsTokenService(
		@Value("\${AZURE_APP_CLIENT_ID}") clientId: String,
		@Value("\${AZURE_APP_CLIENT_SECRET}") clientSecret: String,
		@Value("\${AZURE_OPENID_CONFIG_TOKEN_ENDPOINT}") endpoint: String,
	): CachingAzureAdClientCredentialsTokenService {
		return CachingAzureAdClientCredentialsTokenService(
			AzureAdClientCredentialsService(
				clientId = clientId,
				clientSecret = clientSecret,
				endpoint = endpoint,
				restTemplate = RestTemplate(),
			)
		)
	}
}

fun main(args: Array<String>) {
	runApplication<PensjonOverfoerSpraakFraTpsTilKrrApplication>(*args)
}
