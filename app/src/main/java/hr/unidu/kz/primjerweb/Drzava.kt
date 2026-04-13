package hr.unidu.kz.primjerweb

import kotlinx.serialization.Serializable

@Serializable
data class Drzava(
    val name: String,
    val countryCode: String
){
    override fun toString(): String {
        return name
    }
}
