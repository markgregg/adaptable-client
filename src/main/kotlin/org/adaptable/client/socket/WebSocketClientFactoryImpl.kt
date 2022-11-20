package org.adaptable.client.socket

import org.adaptable.common.api.socket.SocketContainerFactoryImpl
import org.adaptable.common.api.socket.WebSocketClient
import org.adaptable.common.api.socket.WebSocketClientImpl

internal class WebSocketClientFactoryImpl : WebSocketClientFactory {
    override fun createSocketClient() : WebSocketClient {
        return WebSocketClientImpl(
            Util.agentURI(),
            Util.attemptsToConnect(),
            Util.delayBetweenAttemptsToConnect(),
            SocketContainerFactoryImpl() )
    }
}