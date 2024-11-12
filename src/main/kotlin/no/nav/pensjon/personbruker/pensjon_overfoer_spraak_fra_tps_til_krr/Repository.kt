package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.support.TransactionTemplate

@Repository
class Repository(
    val jdbcTemplate: JdbcTemplate,
    val transactionTemplate: TransactionTemplate,
) {
    fun hentPerson(): String? =
        transactionTemplate.execute {
            jdbcTemplate.queryForObject("select fnr from person where lastet_opp is null limit 1", String::class.java)
        }

    fun oppdaterLagretFlagg(person: String, erSatt: Boolean) {
        transactionTemplate.executeWithoutResult {
           jdbcTemplate.update("update person set lastet_opp = ? where fnr = ?", erSatt, person)

        }
    }

    fun antallOpprettet(): Int? = transactionTemplate.execute {
        jdbcTemplate.queryForObject("select count(*) from person where lastet_opp is true", Int::class.java)
    }

    fun antallIgnorert(): Int? = transactionTemplate.execute {
        jdbcTemplate.queryForObject("select count(*) from person where lastet_opp is false", Int::class.java)
    }

    fun antallGjenvaerende(): Int? = transactionTemplate.execute {
        jdbcTemplate.queryForObject("select count(*) from person where lastet_opp is null", Int::class.java)
    }
}
