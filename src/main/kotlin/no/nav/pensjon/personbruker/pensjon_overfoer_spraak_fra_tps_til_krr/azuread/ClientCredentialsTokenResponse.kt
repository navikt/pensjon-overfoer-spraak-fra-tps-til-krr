package no.nav.pensjon.personbruker.pensjon_overfoer_spraak_fra_tps_til_krr.azuread

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.temporal.TemporalAmount

data class ClientCredentialsTokenResponse(
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Long,
    @JsonProperty("access_token") val accessToken: String,
) {
    @JsonIgnore
    val issued = LocalDateTime.now()

    @JsonIgnore
    fun expires(expireRestriction: TemporalAmount): LocalDateTime {
        return issued.plusSeconds(expiresIn).minus(expireRestriction)
    }

    @JsonIgnore
    fun isExpired(atTime: LocalDateTime, expireRestriction: TemporalAmount): Boolean {
        return atTime.isAfter(expires(expireRestriction))
    }
}
