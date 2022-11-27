package io.github.markgregg.client.socket

import io.github.markgregg.common.api.socket.SocketContainerFactoryImpl
import io.github.markgregg.common.api.socket.WebSocketClient
import io.github.markgregg.common.api.socket.WebSocketClientImpl

internal class WebSocketClientFactoryImpl : WebSocketClientFactory {
    override fun createSocketClient() : WebSocketClient {
        return WebSocketClientImpl(
            Util.agentURI(),
            Util.attemptsToConnect(),
            Util.delayBetweenAttemptsToConnect(),
            SocketContainerFactoryImpl() )
    }
}