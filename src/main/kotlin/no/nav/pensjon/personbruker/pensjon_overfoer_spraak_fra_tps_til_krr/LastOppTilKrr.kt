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
                if (personIdent != null && isNpid(personIdent)) {
                    repository.oppdaterLagretFlagg(personIdent, false, false)
                } else if (personIdent != null && !isDnummer(personIdent)) {
                    teller.incrementAndGet()
                    val spraakIKrr = digdirKrrProxyClient.hentSpraak(personIdent)
                    if(spraakIKrr == null) {
                        logger.info("Bruker finnes ikke i krr -> Oppdaterer...")
                        val brukereErSatt = digdirKrrProxyClient.setSpraakForAnalogBruker(personIdent, ENGELSK)
                        repository.oppdaterLagretFlagg(personIdent, brukereErSatt, !brukereErSatt)
                    } else {
                        logger.info("Språk i KRR: $spraakIKrr")
                        repository.oppdaterLagretFlagg(personIdent, false, false)
                    }

                    if (teller.get() % 100 == 0) {
                        val ignorertKrr = repository.antallIgnorert()
                        val antallFeilet = repository.antallFeilet()
                        logger.info("Lastet opp ${teller.get()} språkvalg. Antall ignorert $ignorertKrr. Antall feilet: $antallFeilet")
                    }

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

    private fun isDnummer(fnr: String) = fnr[0].digitToInt() > 3
    private fun isNpid(fnr: String) = fnr[2].digitToInt() > 1
}
