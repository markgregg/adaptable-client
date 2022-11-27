package io.github.markgregg.client.socket

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.github.markgregg.common.api.socket.WebSocketClientImpl

class WebsocketClientFactoryImplTest : FunSpec({

	test("createSocketClient") {
		WebSocketClientFactoryImpl().createSocketClient().shouldBeInstanceOf<WebSocketClientImpl>()
	}
})
