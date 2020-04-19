package com.xenji.xbookmarksync

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/info")
class InfoController(
    @Value("\${maxSyncSize}") private val maxSyncSize: Int,
    @Value("\${version}") private val version: String
) {

    @GetMapping
    fun info() = Mono.just(InfoResponse(maxSyncSize, "", 1, version))
}


data class InfoResponse(
    val maxSyncSize: Int,
    val message: String,
    val status: Short,
    val version: String
)
