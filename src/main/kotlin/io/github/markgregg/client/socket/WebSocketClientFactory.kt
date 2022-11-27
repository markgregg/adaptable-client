package io.github.markgregg.client.socket

import io.github.markgregg.common.api.socket.WebSocketClient

internal interface WebSocketClientFactory {
    fun createSocketClient(): WebSocketClient
}