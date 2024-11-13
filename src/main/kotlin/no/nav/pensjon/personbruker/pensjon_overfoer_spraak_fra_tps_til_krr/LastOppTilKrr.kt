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
    private val ENGELSK = "en"

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun lastOppTilKrr() {
        try {
            val teller = AtomicInteger()

            do {
                val personIdent = repository.hentPerson()
                if (personIdent != null) {
                    teller.incrementAndGet()
                    val spraakIKrr = digdirKrrProxyClient.hentSpraak(personIdent)
                    if(spraakIKrr == null) {
                        logger.info("Bruker finnes ikke i krr -> Oppdaterer...")
                        val brukereErSatt = digdirKrrProxyClient.setSpraakForAnalogBruker(personIdent, ENGELSK)
                        repository.oppdaterLagretFlagg(personIdent, brukereErSatt)
                    } else {
                        repository.oppdaterLagretFlagg(personIdent, false)
                    }

                    if (teller.get() % 100 == 0) {
                        val ignorertKrr = repository.antallIgnorert()
                        logger.info("Lastet opp ${teller.get()} språkvalg. Antall ignorert $ignorertKrr")
                    }

                } else {
                    logger.info("Ferdig med å laste opp til krr")
                }
            } while (personIdent != null)

            val opprettetKrr = repository.antallOpprettet()
            val ignorertKrr = repository.antallIgnorert()
            val gjenvaerende = repository.antallGjenvaerende()
            logger.info("Ferdig. Antall opprettet i KRR: $opprettetKrr, antall ignorert: $ignorertKrr, antall gjenvaerende: $gjenvaerende")

        } catch (empty: EmptyResultDataAccessException) {
            logger.info("Tom tabell")
        } catch (e: Exception) {
            logger.error("Feil ved opplastning til KRR", e)
        }
    }
}
