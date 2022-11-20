package org.adaptable.client.socket

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.adaptable.common.api.socket.WebSocketClientImpl

class WebsocketClientFactoryImplTest : FunSpec({

	test("createSocketClient") {
		WebSocketClientFactoryImpl().createSocketClient().shouldBeInstanceOf<WebSocketClientImpl>()
	}
})
