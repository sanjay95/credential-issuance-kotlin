package credentialissuance

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File

@Serializable
data class EmergencyContact(
    val name: String,
    val phone: String,
    val email: String,
    val relation: String
)
@Serializable
data class User(
    val userId: String,
    val name: String,
    val gender: String,
    val email: String,
    val phone: String,
    val heightInch: String,
    val emergencyContacts: List<EmergencyContact>
)

@Serializable
data class CurrentCondition(
    val conditionId: String,
    val name: String,
    val description: String,
    val severity: String
)

@Serializable
data class AggregateHealthData(
    val vitalAggregateID: String,
    val aggregationDate: String,
    val measurementType: String,
    val organizationId: String,
    val user: User,
    val currentConditions: List<CurrentCondition>,
    val currBmiValue: Double,
    val weightDailyAvgLbs: Double,
    val bpSysUom: String,
    val bpDiaUom: String,
    val bpSys1DayTrend: String,
    val bpDia1DayTrend: String,
    val bpSys1DayAvg: Double,
    val bpDia1DayAvg: Double,
    val bpSys7DayAvg: Double,
    val bpDia7DayAvg: Double,
    val bpSys30DayAvg: Double,
    val bpDia30DayAvg: Double,
    val bpSys90DayAvg: Double,
    val bpDia90DayAvg: Double,
    val bpSys180DayAvg: Double,
    val bpDia180DayAvg: Double,
    val bpSys360DayAvg: Double,
    val bpDia360DayAvg: Double
)

fun loadPostRequest(jsonFilePath: String): AggregateHealthData {
    val jsonString = File(jsonFilePath).readText()
    return Json.decodeFromString(serializer<AggregateHealthData>(), jsonString)
}
