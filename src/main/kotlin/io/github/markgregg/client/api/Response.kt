package io.github.markgregg.client.api

import io.github.markgregg.common.api.Response

abstract class Response {
    internal abstract fun toDom(): Response
}