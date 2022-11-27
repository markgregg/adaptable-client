package io.github.markgregg.client.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.ByteBuffer

class BinaryResponseTest : FunSpec({

	test("toDom") {
		val domResponse = BinaryResponse(200, ByteBuffer.wrap("hello".toByteArray())).toDom() as io.github.markgregg.common.web.BinaryResponse

		domResponse.status shouldBe 200
		domResponse.payload() shouldBe  "hello".toByteArray()
		domResponse.headers shouldBe emptyMap()
	}
})
