package credentialissuance

import com.affinidi.tdk.authprovider.AuthProvider
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import io.github.cdimascio.dotenv.Dotenv

@Service
class Utils() {

    private val dotenv: Dotenv = Dotenv.load()


    private val webClient =
        WebClient.builder()
            .baseUrl(System.getenv("API_GATEWAY_URL"))
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();


    private fun getAuthProvider(): AuthProvider {
        val params = mutableMapOf<String, String>()
        params["projectId"] = System.getenv("PROJECT_ID")!!
        params["tokenId"] = System.getenv("TOKEN_ID")!!
        params["keyId"] = System.getenv("KEY_ID")!!
        params["privateKey"] = System.getenv("PRIVATE_KEY")!!.replace("\\n", System.lineSeparator())
        params["passphrase"] = System.getenv("PASSPHRASE")!!

        return AuthProvider(params)
    }

    fun generatePST(): String {
        val authProvider = getAuthProvider()
        val projectScopedToken = authProvider.fetchProjectScopedToken()
        println("Project Scoped Token: $projectScopedToken")
        return projectScopedToken
    }

    fun getIotaJWT(userDID: String): String {
        val authProvider = getAuthProvider()
        val iotaConfigId = System.getenv("IOTA_CONFIG_ID")!!

        val tokenOutput = authProvider.createIotaToken(iotaConfigId, userDID)
        val jwt = tokenOutput.iotaJwt
        val sessionId = tokenOutput.iotaSessionId

        println("Iota JWT: $jwt")
        println("Iota sessionId: $sessionId")

        return jwt
    }

    fun startIssuance(userDID: String): Mono<Map<String, Any>> {
        val apiEndpoint = String.format("/cis/v1/%s/issuance/start", System.getenv("PROJECT_ID")!!)

        val user = mutableMapOf<String, Any>()
        user["name"] = "John"
        user["email"] = "jkk@.dk.dk"
        user["age"] = "25"
        user["userId"] = "1"
        user["isActive"] = "true"
        user["address"] = mapOf(
            "street" to "Main Street",
            "city" to "New York",
            "zip" to "10001"
        )

        val vitals = mutableMapOf<String, Any>()
        vitals["height"] = "180"
        vitals["weight"] = "75"
        vitals["bloodType"] = "A+"
        vitals["conditions"] = "asthma ,diabetes"
        vitals["lastCheckup"] = "2019-01-01"
        vitals["nextCheckup"] = "2020-01-01"
        vitals["insurance"] = mapOf(
            "provider" to "Blue Cross",
            "policyNumber" to "1234567890"
        )
        vitals["bloodPressure"] = mapOf(
            "systolic" to "120",
            "diastolic" to "80"
        )
        vitals["heartRate"] = mapOf(
            "rhythm" to "normal",
            "restingHr" to "60",
            "maxHr" to "90",
            "minHr" to "58",
            "avgHr" to "70",
            "duration" to "8 hours"
        )
        vitals["temperature"] = 37.0
        vitals["respiratoryRate"] = 16
        vitals["oxygenSaturation"] = 98.0
        vitals["glucose"] = 100.0
        vitals["cholesterol"] = mapOf(
            "total" to 200,
            "hdl" to 50,
            "ldl" to 150
        )
        vitals["electrocardiogram"] = mapOf(
            "rate" to 75,
            "rhythm" to "normal",
            "intervals" to mapOf(
                "p" to "normal",
                "qr" to "normal",
                "qrs" to "normal",
                "qt" to "normal"
            ),
            "axis" to mapOf(
                "qrs" to "normal",
                "p" to "normal",
                "t" to "normal"
            ),
            "waveforms" to mapOf(
                "p" to "normal",
                "qrs" to "normal",
                "t" to "normal"
            )
        )

        val credentialData = mutableMapOf<String, Any>()
        credentialData["user"] = user
        credentialData["vitals"] = vitals

        val requestBody = mutableMapOf<String, Any>()
        requestBody["data"] = listOf(
            mapOf(
                "credentialTypeId" to "healthProfile",
                "credentialData" to credentialData
            )
        )
        requestBody["claimMode"] = "NORMAL"
        requestBody["holderDid"] = userDID

        println(requestBody)

        val projectScopedToken = generatePST()
        val headers = mapOf("Authorization" to "Bearer $projectScopedToken")

        val response = sendPostRequest(apiEndpoint, headers, requestBody)

        println("Request Successful, response: $response")

        return response
    }

    private fun sendPostRequest(
        apiEndpoint: String,
        headers: Map<String, String>,
        requestBody: Any
    ): Mono<Map<String, Any>> {
        return webClient.post()
            .uri(apiEndpoint)
            .headers {
                headers.forEach { (key, value) ->
                    it.set(key, value)
                }
            }
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .onErrorResume(WebClientResponseException::class.java) { e ->
                System.err.println("WebClientResponseException: ${e.responseBodyAsString}")
                Mono.empty() // Or handle error as needed
            }
    }
}