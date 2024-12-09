package credentialissuance


import com.affinidi.tdk.authprovider.AuthProvider
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import io.github.cdimascio.dotenv.Dotenv
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Service
class IssuanceService() {
    private val logger: Logger = LoggerFactory.getLogger(IssuanceController::class.java)

    private val dotenv: Dotenv = Dotenv.load()

    private val webClient =
        WebClient.builder()
            .baseUrl(dotenv["API_GATEWAY_URL"])
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build()



    fun startIssuance(userDID: String): Mono<StartIssuanceResponse> {
        val apiEndpoint = String.format("/cis/v1/%s/issuance/start", dotenv["PROJECT_ID"]!!)
        val requestData = loadPostRequest("src/main/resources/data/aggregateData1.json");
        val requestBody = mutableMapOf<String, Any>()
        requestBody["data"] = listOf(
            mapOf(
                "credentialTypeId" to dotenv["CREDENTIAL_TYPE_ID"],
                "credentialData" to requestData
            )
        )
        requestBody["claimMode"] = "NORMAL"
        requestBody["holderDid"] = userDID

        val projectScopedToken = generatePST()
        val headers = mapOf("Authorization" to "Bearer $projectScopedToken")
        val response = sendPostRequest(apiEndpoint, headers, requestBody)
        response.subscribe { jsonResponse ->
            val jsonString = Json.encodeToString(jsonResponse)
            logger.info("Request Successful, response: $jsonString")
        }

        return response
    }

    private fun sendPostRequest(
        apiEndpoint: String,
        headers: Map<String, String>,
        requestBody: Any
    ): Mono<StartIssuanceResponse> {
        logger.info("apiEndpoint  apiEndpoint : $apiEndpoint")

        return webClient.post()
            .uri(apiEndpoint)
            .headers {
                headers.forEach { (key, value) ->
                    it.set(key, value)
                }
            }
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(StartIssuanceResponse::class.java)
            .onErrorResume(WebClientResponseException::class.java) { e ->
                System.err.println("WebClientResponseException: ${e.responseBodyAsString}")
                Mono.empty() // Or handle error as needed
            }
    }

    private fun getAuthProvider(): AuthProvider {
        val params = mutableMapOf<String, String>()

        params["projectId"] = dotenv["PROJECT_ID"]!!

        params["tokenId"] = dotenv["TOKEN_ID"]!!
        params["keyId"] = dotenv["KEY_ID"]!!
        params["privateKey"] = dotenv["PRIVATE_KEY"]!!.replace("\\n", System.lineSeparator())
        params["passphrase"] = dotenv["PASSPHRASE"]!!

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
        val iotaConfigId = dotenv["IOTA_CONFIG_ID"]!!

        val tokenOutput = authProvider.createIotaToken(iotaConfigId, userDID)
        val jwt = tokenOutput.iotaJwt
        val sessionId = tokenOutput.iotaSessionId

        println("Iota JWT: $jwt")
        println("Iota sessionId: $sessionId")

        return jwt
    }

}

