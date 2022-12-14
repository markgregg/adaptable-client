package io.github.markgregg.client.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.github.markgregg.client.api.requests.RequestsImpl
import io.github.markgregg.common.web.WebRequest
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import java.util.*

class EndPointTest : FunSpec({

	test("parent") {
		val parent = Test().EndPointController()
		val endPoint = io.github.markgregg.client.api.EndPoint("Id", parent, RequestsImpl())
		endPoint.parent shouldBe parent
	}

	test("rules") {
		val endPoint = io.github.markgregg.client.api.EndPoint("Id", Test().EndPointController(), RequestsImpl())
		endPoint.addRule(NoRule(TextResponse(200, "test")))
		endPoint.rules().size shouldBe 1
	}

	test("requests") {
		val endPoint = io.github.markgregg.client.api.EndPoint("Id", Test().EndPointController(), RequestsImpl())
		endPoint.addRequest(
			WebRequest(
				UUID.randomUUID(),
				emptyMap(),
				emptyMap(),
				"{\"test\": \"value\"}"
			)
		)
		endPoint.requests().requests().size shouldBe 1
	}


	test("makeUnavailable") {
		val parent = mock(Parent::class.java)
		val endPoint = io.github.markgregg.client.api.EndPoint("id", parent, RequestsImpl())
		endPoint.makeUnavailable()
		verify(endPoint.parent)!!.makeEndPointUnavailable(eq("id"))
	}

	test("makeAvailable") {
		val parent = mock(Parent::class.java)
		val endPoint = io.github.markgregg.client.api.EndPoint("id", parent, RequestsImpl())
		endPoint.makeAvailable()
		verify(endPoint.parent)!!.makeEndPointAvailable(eq("id"))
	}

	test("toDom") {
		val endPointDom = io.github.markgregg.client.api.EndPoint("id", Test().EndPointController(), RequestsImpl())
			.addRule(StandardRule(
				"\$body.test=='value'",
				TextResponse(200, "hello"),
			))
			.toDom()
		endPointDom.id shouldBe "id"
		endPointDom.rules?.size shouldBe 1
	}
})
