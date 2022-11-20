package org.adaptable.client.socket

import org.adaptable.common.api.socket.WebSocketClient

internal interface WebSocketClientFactory {
    fun createSocketClient(): WebSocketClient
}