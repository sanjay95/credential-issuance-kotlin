package credentialissuance

import com.affinidi.tdk.authProvider.AuthProvider
import com.affinidi.tdk.common.VaultUtil
import com.affinidi.tdk.credential.issuance.client.ApiClient
import com.affinidi.tdk.credential.issuance.client.apis.IssuanceApi
import com.affinidi.tdk.credential.issuance.client.auth.ApiKeyAuth
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInput
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInput.ClaimModeEnum
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInputDataInner
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceResponse
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

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

        val holderDid = userDID
        val credentialType = dotenv["CREDENTIAL_TYPE_ID"]
        val credentialData: MutableMap<String, Any> = mutableMapOf()
        credentialData["firstName"] = "Jhon"
        credentialData["lastName"] = "Doe"
        credentialData["whyHive"] = "Testing"
        credentialData["writtenAssessment"] = "Is it needed"
        credentialData["assessmentDate"] = "2021-09-01"

        // Create a client for issuance
        val issuanceClient = ApiClient()
        val issueTokenAuth = issuanceClient.getAuthentication("ProjectTokenAuth") as ApiKeyAuth

        // Create a authentication token
        val projectToken = getAuthProvider().fetchProjectScopedToken()
        issueTokenAuth.setApiKey(projectToken)

        // Iniialize the API client
        val issuanceApi = IssuanceApi(issuanceClient)

        // Create input for issuance service
        val startIssuanceInput =
                StartIssuanceInput()
                        .holderDid(holderDid)
                        .claimMode(ClaimModeEnum.NORMAL)
                        .data(
                                mutableListOf(
                                        StartIssuanceInputDataInner()
                                                .credentialTypeId(credentialType)
                                                .credentialData(credentialData)
                                )
                        )

        // Issue the credential using the data above
        val response = issuanceApi.startIssuance(dotenv["PROJECT_ID"], startIssuanceInput)
        System.out.println(
                "Credential Offer Generated ********** " + response.getCredentialOfferUri() + "\n\n"
        )

        response.setCredentialOfferUri(VaultUtil.buildClaimLink(response.getCredentialOfferUri()))
        System.out.println(
                "Consumable Vault Claim Link specific to your environment ********** " +
                        response.getCredentialOfferUri()
        )

        return Mono.just(response)
    }

    // private fun sendPostRequest(
    //     apiEndpoint: String,
    //     headers: Map<String, String>,
    //     requestBody: Any
    // ): Mono<StartIssuanceResponselocal> {
    //     logger.info("apiEndpoint  apiEndpoint : $apiEndpoint")

    //     return webClient.post()
    //         .uri(apiEndpoint)
    //         .headers {
    //             headers.forEach { (key, value) ->
    //                 it.set(key, value)
    //             }
    //         }
    //         .bodyValue(requestBody)
    //         .retrieve()
    //         .bodyToMono(StartIssuanceResponse::class.java)
    //         .onErrorResume(WebClientResponseException::class.java) { e ->
    //             System.err.println("WebClientResponseException: ${e.responseBodyAsString}")
    //             Mono.empty() // Or handle error as needed
    //         }
    // }

    private fun getAuthProvider(): AuthProvider {
        //    val authProvider = AuthProvider.Configurations()
        //     .projectId(dotenv["PROJECT_ID"]!!)
        //     .tokenId(dotenv["TOKEN_ID"]!!)
        //     .privateKey(dotenv["PRIVATE_KEY"]!!.replace("\\n", System.lineSeparator()))
        //     .keyId(dotenv["KEY_ID"]!!)
        //     .passphrase(dotenv["PASSPHRASE"]!!)
        //     .build()
        // return authProvider

        return AuthProvider.Configurations().buildWithEnv()
    }

    fun generatePST(): String {
        val authProvider = getAuthProvider()
        val projectScopedToken = authProvider.fetchProjectScopedToken()
        println("Project Scoped Token: $projectScopedToken")
        return projectScopedToken
    }

    // fun getIotaJWT(userDID: String): String {
    //     val authProvider = getAuthProvider()
    //     val iotaConfigId = dotenv["IOTA_CONFIG_ID"]!!

    //     val tokenOutput = authProvider.createIotaToken(iotaConfigId, userDID)
    //     val jwt = tokenOutput.iotaJwt
    //     val sessionId = tokenOutput.iotaSessionId

    //     println("Iota JWT: $jwt")
    //     println("Iota sessionId: $sessionId")

    //     return jwt
    // }

}
