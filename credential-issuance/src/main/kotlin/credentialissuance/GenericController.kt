package credentialissuance

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class GenericController(private val service: MessageService) {
    private val logger: Logger = LoggerFactory.getLogger(GenericController::class.java)

    @GetMapping("/VAULT_URL")
    fun get(): ResponseEntity<String> {

        val env = service.findEnv()

        // Return the saved message as is
        return ResponseEntity.ok(env)
    }
}



