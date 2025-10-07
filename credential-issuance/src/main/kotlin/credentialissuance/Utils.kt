package credentialissuance

import com.affinidi.tdk.authProvider.AuthProvider
import com.affinidi.tdk.credential.issuance.client.ApiClient
import com.affinidi.tdk.credential.issuance.client.apis.IssuanceApi
import com.affinidi.tdk.credential.issuance.client.auth.ApiKeyAuth
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInput
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInputDataInner
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.Dotenv
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.Security
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
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
                val credentialType: String = dotenv["CREDENTIAL_TYPE_ID"]
                // val credentialType=null
                // val credentialData: MutableMap<String, Any> = mutableMapOf()
                var credentialRawData: String =
                        "" // File("credential-issuance/src/main/resources/data/aggregateData1.json").readText()
                val resource = this::class.java.classLoader.getResource("data/aggregateData1.json")
                if (resource != null) {
                        val path = Paths.get(resource.toURI())
                        credentialRawData = Files.readString(path)
                }
                val objectMapper = ObjectMapper()
                val data: Map<String, Any> =
                        objectMapper.readValue(
                                credentialRawData,
                                object :
                                        com.fasterxml.jackson.core.type.TypeReference<
                                                MutableMap<String, Any>>() {}
                        )

                var credentialData: MutableMap<String, Any> = HashMap()
                credentialData.putAll(data)
                println("Credential Data: $credentialData")

                // Create a client for issuance
                val issuanceClient = ApiClient()
                val issueTokenAuth =
                        issuanceClient.getAuthentication("ProjectTokenAuth") as ApiKeyAuth

                // Create a authentication token
                val projectToken = getAuthProvider().fetchProjectScopedToken()
                issueTokenAuth.setApiKey(projectToken)

                // Iniialize the API client
                val issuanceApi = IssuanceApi(issuanceClient)

                // Create input for issuance service
                val startIssuanceInput =
                        StartIssuanceInput()
                                .holderDid(holderDid)
                                // .claimMode(ClaimModeEnum.TX_CODE)
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
                        "Credential Offer Generated ********** " +
                                response.getCredentialOfferUri() +
                                "\n\n"
                )

                response.setCredentialOfferUri(
                        // VaultUtil.buildClaimLink(response.getCredentialOfferUri())
                        buildClaimLinkWithRedirect(response.getCredentialOfferUri())
                )
                System.out.println(
                        "Consumable Vault Claim Link specific to your environment ********** " +
                                response.getCredentialOfferUri()
                )

                return Mono.just(response)
        }

        private fun buildClaimLinkWithRedirect(credentialOfferUri: String): String {
                var webVaultUrl = dotenv["VAULT_URL"]!!
                val redirectUri = dotenv["REDIRECT_URI"]!!
                if (!credentialOfferUri.equals("") &&
                                !webVaultUrl.equals("") &&
                                !redirectUri.equals("")
                ) {
                        var claimURL: String =
                                UriComponentsBuilder.fromUriString(webVaultUrl)
                                        .queryParam(
                                                "credential_offer_uri",
                                                URLEncoder.encode(
                                                        credentialOfferUri,
                                                        StandardCharsets.UTF_8.toString()
                                                )
                                        )
                                        .queryParam(
                                                "return_uri",
                                                URLEncoder.encode(
                                                        redirectUri,
                                                        StandardCharsets.UTF_8.toString()
                                                )
                                        )
                                        .build()
                                        .toUriString()

                        println("Claim URL: $claimURL")
                        println("Claim URL zto String: ${claimURL.toString()}")
                        // println("Claim URL toASCIIString: ${claimURL.toASCIIString()}")

                        return claimURL
                } else {
                        System.out.println(
                                "Invalid Credential Offer URI passed to utility $credentialOfferUri. Returning vault url"
                        )
                        return webVaultUrl
                }
        }

        private fun getAuthProvider(): AuthProvider {

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

        fun listSecurityProviders() {
                for (provider in Security.getProviders()) {
                        println("Provider: ${provider.name}")
                        for (service in provider.services) {
                                println("  Algorithm: ${service.algorithm}")
                        }
                }
        }
}
