package credentialissuance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class CredentialIssuanceApplication

fun main(args: Array<String>) {
    runApplication<CredentialIssuanceApplication>(*args)
}
