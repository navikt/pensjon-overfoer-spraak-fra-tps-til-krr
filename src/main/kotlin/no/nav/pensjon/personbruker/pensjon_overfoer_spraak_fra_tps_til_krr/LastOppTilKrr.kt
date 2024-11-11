package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Component
class LastOppTilKrr(
    val repository: Repository,
    val digdirKrrProxyClient: DigdirKrrProxyClient,
) {
    private val logger: Logger = getLogger(javaClass)

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun lastOppTilKrr() {
        try {
            val teller = AtomicInteger()

            do {
                val personIdent = repository.hentPerson()
                if (personIdent != null) {
                    val spraakKrr = digdirKrrProxyClient.hentSpraak(personIdent)
                    if (spraakKrr == "en") {
                        logger.info("Bruker har allerede engelsk i KRR. Setter bruker til ferdig")
                        repository.oppdaterLagretFlagg(personIdent)
                        continue
                    }
                    teller.incrementAndGet()
                    digdirKrrProxyClient.setSpraakForAnalogBruker(personIdent, "en")
                    if (teller.get() % 100 == 0) {
                        logger.info("Lastet opp {} språkvalg til krr", teller.get())
                    }
                    repository.oppdaterLagretFlagg(personIdent)
                } else {
                    logger.info("Ferdig med å laste opp til krr")
                }
            } while (personIdent != null)

            logger.info("Lastet opp {} språkvalg til krr", teller.get())
        } catch (empty: EmptyResultDataAccessException) {
            logger.info("Tom tabell")
        } catch (e: Exception) {
            logger.error("Feil ved opplastning til KRR", e)
        }
    }
}
