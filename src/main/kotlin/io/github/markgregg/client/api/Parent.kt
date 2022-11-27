package io.github.markgregg.client.api

import io.github.markgregg.common.protocol.Message


internal interface Parent {
    fun makeEndPointUnavailable(endPoint: String)
    fun makeEndPointAvailable(endPoint: String)
    fun sendMessage(message: Message<*>)
}
