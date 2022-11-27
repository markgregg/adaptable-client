package io.github.markgregg.client.socket

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCaseOrder
import io.kotest.matchers.shouldBe
import io.github.markgregg.client.api.exceptions.ClientInitializerNotFoundException
import java.net.URI

class UtilTest : FunSpec({

	test("test") {
		Util.namespaces = arrayOf("io.github.markgregg.client.api")

		shouldThrowExactly<ClientInitializerNotFoundException> {
			Util.agentURI() shouldBe URI("test")
		}
	}

	test("agentURI") {
		Util.namespaces = emptyArray()
        Util.agentURI() shouldBe URI("ws://test:8080/agentClient/api")
	}

	test("attemptsToConnect") {
		Util.attemptsToConnect() shouldBe 5
	}

	test("delayBetweenAttemptsToConnect") {
		Util.delayBetweenAttemptsToConnect() shouldBe 20000
	}

}) {
	override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential
}
