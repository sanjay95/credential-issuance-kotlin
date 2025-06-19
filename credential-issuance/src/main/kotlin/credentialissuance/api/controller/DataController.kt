package credentialissuance.api.controller // Adjust package as needed

import credentialissuance.api.model.Item
import credentialissuance.api.service.DataService
import credentialissuance.api.service.OmitIdItem // Or import from a dto package if moved
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

    @GetMapping("/{id}")
    fun getDataById(@PathVariable id: String): Mono<ResponseEntity<Item>> {
        return Mono.fromCallable { dataService.getItemById(id) }
                .map { ResponseEntity.ok(it!!) }
                .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun addData(@RequestBody itemRequest: OmitIdItem): Mono<ResponseEntity<Item>> {
        return Mono.fromCallable { dataService.addItem(itemRequest) }.map {
            ResponseEntity.status(HttpStatus.CREATED).body(it)
        }
    }

    @PutMapping("/{id}")
    fun updateData(
            @PathVariable id: String,
            @RequestBody itemUpdate: OmitIdItem
    ): Mono<ResponseEntity<Item>> {
        return Mono.fromCallable { dataService.updateItem(id, itemUpdate) }.flatMap { updatedItem ->
            if (updatedItem != null) {
                Mono.just(ResponseEntity.ok(updatedItem))
            } else {
                Mono.just(ResponseEntity.notFound().build<Item>())
            }
        }
    }

    @DeleteMapping("/{id}")
    fun deleteData(@PathVariable id: String): Mono<ResponseEntity<Void>> {
        return Mono.fromCallable { dataService.deleteItem(id) }.map { deleted ->
            if (deleted) {
                ResponseEntity.noContent().build<Void>()
            } else {
                ResponseEntity.notFound().build<Void>()
            }
        }
    }
}
