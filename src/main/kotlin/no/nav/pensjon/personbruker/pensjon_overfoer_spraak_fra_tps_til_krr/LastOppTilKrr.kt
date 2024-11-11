package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
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
                val person = repository.hentPerson()
                if (person != null) {
                    teller.incrementAndGet()
                    digdirKrrProxyClient.setSpraakForAnalogBruker(person, "en")
                    if (teller.get() % 100 == 0) {
                        logger.info("Lastet opp {} språkvalg til krr", teller.get())
                    }
                    repository.oppdaterLagretFlagg(person)
                } else {
                    logger.info("Ferdig med å laste opp til krr")
                }
            } while (person != null)

            logger.info("Lastet opp {} språkvalg til krr", teller.get())
        } catch (e: Exception) {
            logger.error("Feil ved opplastning til KRR", e)
        }
    }
}
