package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import com.github.benmanes.caffeine.cache.LoadingCache
import org.slf4j.LoggerFactory
import java.time.Duration.of
import java.time.LocalDateTime.*
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.NANOS
import java.time.temporal.TemporalAmount

class CachingAzureAdClientCredentialsTokenService(
    private val azureAdClientCredentialsTokenService: AzureAdClientCredentialsTokenService
) : AzureAdClientCredentialsTokenService {

    private val logger = LoggerFactory.getLogger(CachingAzureAdClientCredentialsTokenService::class.java)

    private val tokenCache: LoadingCache<List<String>, ClientCredentialsTokenResponse> = Caffeine.newBuilder()
        .expireAfter(object : Expiry<List<String>, ClientCredentialsTokenResponse> {
            override fun expireAfterCreate(scope: List<String>, graph: ClientCredentialsTokenResponse, currentTime: Long) = NANOS.between(now(), graph.expires(
                expireRestriction
            ))
            override fun expireAfterUpdate(scope: List<String>, graph: ClientCredentialsTokenResponse, currentTime: Long, currentDuration: Long) = currentDuration
            override fun expireAfterRead(scope: List<String>, graph: ClientCredentialsTokenResponse, currentTime: Long, currentDuration: Long) = currentDuration
        })
        .build { scope -> azureAdClientCredentialsTokenService.fetch(scope) }

    override fun fetch(scope: List<String>): ClientCredentialsTokenResponse = tokenCache.get(scope)
        ?.let {
            val now = now()
            if (it.isExpired(now, expireRestriction)) {
                logger.warn("Got an expired token from the cache, now=${now()}, issued=${it.issued}, expires=${it.expires(
                    expireRestriction
                )}, expiresIn=${it.expiresIn}, expireRestriction=$expireRestriction")
                azureAdClientCredentialsTokenService.fetch(scope)
            } else {
                it
            }
        }
        ?: run {
            logger.warn("Got null token from cache")
            azureAdClientCredentialsTokenService.fetch(scope)
        }

    companion object {
        val expireRestriction: TemporalAmount = of(5, MINUTES)
    }
}
