package credentialissuance

import com.affinidi.tdk.authProvider.AuthProvider
import com.affinidi.tdk.credential.issuance.client.ApiClient
import com.affinidi.tdk.credential.issuance.client.apis.IssuanceApi
import com.affinidi.tdk.credential.issuance.client.auth.ApiKeyAuth
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInput
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInput.ClaimModeEnum
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
                                // .holderDid(holderDid)
                                .claimMode(ClaimModeEnum.TX_CODE)
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

                val key =
                        "-----BEGIN ENCRYPTED PRIVATE KEY-----\n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\n-----END ENCRYPTED PRIVATE KEY-----\n".replace(
                                "\n",
                                "\\n"
                        )
                val authProvider =
                        AuthProvider.Configurations()
                                .projectId("cbdea4ac-0c07-44de-b42f-2cb6a6a2f8dc")
                                .tokenId("314cba2b-943a-4d11-ad53-bbf3729eff4e")
                                .privateKey(key)
                                .keyId("314cba2b-943a-4d11-ad53-bbf3729eff4e")
                                .passphrase("secure")
                                .build()
                return authProvider

                // return AuthProvider.Configurations().buildWithEnv()
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
