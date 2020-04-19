package com.xenji.xbookmarksync

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

@RestController
@RequestMapping("/bookmarks")
class BookmarksController(
    @Value("\${version}") private val version: String,
    @Value("\${tableName}") private val tableName: String,
    private val dynamoDb: AmazonDynamoDB
) {
    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    @PostMapping
    fun create(@RequestBody createRequest: CreateRequest): Mono<CreateResponse> =
        Mono.fromSupplier {
            val id = generateId()
            val now = ZonedDateTime.now().format(DATE_FORMAT)
            dynamoDb.putItem(tableName, mapOf(
                "id" to AttributeValue(id),
                "lastUpdated" to AttributeValue(now),
                "version" to AttributeValue(version)
            ))
            CreateResponse(id, now, version)
        }


    @GetMapping("{id}")
    fun get(@PathVariable id: String): Mono<GetResponse> {
        val response = dynamoDb.getItem(tableName, mapOf("id" to AttributeValue(id)), true)
        return if (response.item != null && response.item["bookmarks"] != null) {
            Mono.just(GetResponse(
                response.item["bookmarks"]?.s!!,
                response.item["lastUpdated"]?.s!!,
                response.item["version"]?.s!!
            ))
        } else {
            throw BookmarksNotFoundException()
        }
    }

    @PutMapping("{id}")
    fun put(@PathVariable id: String, @RequestBody body: PutRequest): Mono<PutResponse> =
        Mono.fromSupplier {
            dynamoDb.putItem(tableName, mapOf(
                "id" to AttributeValue(id),
                "lastUpdated" to AttributeValue(body.lastUpdated),
                "version" to AttributeValue(version),
                "bookmarks" to AttributeValue(body.bookmarks)
            ))
            PutResponse(body.lastUpdated)
        }


    @GetMapping("{id}/lastUpdated")
    fun getLastUpdated(@PathVariable id: String): Mono<GetLastUpdatedResponse> {
        val response = dynamoDb.getItem(tableName, mapOf("id" to AttributeValue(id)), true)
        return if (response.item != null && response.item["lastUpdated"] != null) {
            Mono.just(GetLastUpdatedResponse(response.item["lastUpdated"]?.s!!))
        } else {
            throw BookmarksNotFoundException()
        }
    }

    @GetMapping("{id}/version")
    fun getVersion(@PathVariable id: String): Mono<GetVersionResponse> {
        val response = dynamoDb.getItem(tableName, mapOf("id" to AttributeValue(id)), true)
        return if (response.item != null && response.item["version"] != null) {
            Mono.just(GetVersionResponse(response.item["version"]?.s!!))
        } else {
            throw BookmarksNotFoundException()
        }
    }

    private fun generateId(): String {
        val leftLimit = 48 // numeral '0'
        val rightLimit = 122 // letter 'z'
        val targetStringLength = 32L
        val random = ThreadLocalRandom.current()

        return random.ints(leftLimit, rightLimit + 1)
            .filter { i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97) }
            .limit(targetStringLength)
            .collect({ StringBuilder() },
                { t, value -> t.appendCodePoint(value) },
                { t, u -> t.append(u) }
            )
            .toString()
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class BookmarksNotFoundException() : Exception()

data class CreateRequest(val version: String)
data class CreateResponse(val id: String, val lastUpdated: String, val version: String)
data class GetResponse(val lastUpdated: String, val version: String, val bookmarks: String)
data class PutRequest(val lastUpdated: String, val bookmarks: String)
data class PutResponse(val lastUpdated: String)
data class GetLastUpdatedResponse(val lastUpdated: String)
data class GetVersionResponse(val version: String)
