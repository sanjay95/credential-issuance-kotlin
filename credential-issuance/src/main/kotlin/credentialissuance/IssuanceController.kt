package credentialissuance

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceResponse;

@RestController
@RequestMapping("/issuance")
class IssuanceController(private val service: IssuanceService) {
    private val logger: Logger = LoggerFactory.getLogger(IssuanceController::class.java)

    @PostMapping
    fun post(@RequestBody userDID: UserDID): ResponseEntity<StartIssuanceResponse> {
        logger.info("Received userDID: ${userDID.userDID}")
        val savedMessage = service.startIssuance(userDID.userDID).block()

        // Return the saved message as is
        return ResponseEntity.ok(savedMessage)
    }
}



