package credentialissuance.api.controller // Adjust package as needed

import credentialissuance.api.model.Item
import credentialissuance.api.service.DataService
import credentialissuance.api.service.DataService.CreateItemRequest
import credentialissuance.api.service.DataService.UpdateItemRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/data") // Base path for these endpoints
class DataController(private val dataService: DataService) {

    @GetMapping
    fun getAllData(): Flux<Item> {
        return Flux.fromIterable(dataService.getAllItems())
    }

    @GetMapping("/did/{did}")
    fun getDataByDid(@PathVariable did: String): Mono<ResponseEntity<Item>> {
        return Mono.fromCallable { dataService.getItemByDid(did) }
                .map { item ->
                    if (item != null) ResponseEntity.ok(item) else ResponseEntity.notFound().build()
                }
                .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @GetMapping("/uuid/{uuid}")
    fun getDataByUuid(@PathVariable uuid: String): Mono<ResponseEntity<Item>> {
        return Mono.fromCallable { dataService.getItemByUuid(uuid) }
                .map { item ->
                    if (item != null) ResponseEntity.ok(item) else ResponseEntity.notFound().build()
                }
                .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @GetMapping("/name/{name}")
    fun getDataByName(@PathVariable name: String): Flux<Item> {
        return Flux.fromIterable(dataService.getItemsByName(name))
    }

    @PostMapping
    fun addData(@RequestBody itemRequest: CreateItemRequest): Mono<ResponseEntity<Item>> {
        return Mono.fromCallable { dataService.addItem(itemRequest) }.flatMap { newItem ->
            if (newItem != null) {
                Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(newItem))
            } else {
                // Handle case where DID might already exist (or other creation failure)
                Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build<Item>())
            }
        }
    }

    @PutMapping("/uuid/{uuid}")
    fun updateData(
            @PathVariable uuid: String,
            @RequestBody itemUpdate: UpdateItemRequest
    ): Mono<ResponseEntity<Item>> {
        return Mono.fromCallable { dataService.updateItem(uuid, itemUpdate) }.flatMap { updatedItem
            ->
            if (updatedItem != null) {
                Mono.just(ResponseEntity.ok(updatedItem))
            } else {
                Mono.just(ResponseEntity.notFound().build<Item>())
            }
        }
    }

    @DeleteMapping("/did/{did}")
    fun deleteData(@PathVariable did: String): Mono<ResponseEntity<Void>> {
        return Mono.fromCallable { dataService.deleteItem(did) }.map { deleted ->
            if (deleted) {
                ResponseEntity.noContent().build<Void>()
            } else {
                ResponseEntity.notFound().build<Void>()
            }
        }
    }
}
