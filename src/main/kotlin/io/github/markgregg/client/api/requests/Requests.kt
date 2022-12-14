package io.github.markgregg.client.api.requests


import io.github.markgregg.client.api.Response
import io.github.markgregg.common.api.Request

interface Requests {
    fun add(request: Request): Response?
    fun requests(): List<Request>
    fun <T : Request> response(ofType: Class<T>, forCondition: (T) -> Boolean): ResponseRule<T>
    fun response(expression: String): ResponseRule<Request>
    fun <T : Request> waitFor(ofType: Class<T>, forCondition: (T) -> Boolean, timeInMilliseconds: Long?): T
    fun waitFor(expression: String, timeInMilliseconds: Long? = null): Request
    fun hasResponses(): Boolean
}