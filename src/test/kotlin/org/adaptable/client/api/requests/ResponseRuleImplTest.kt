package org.adaptable.client.api.requests

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.adaptable.client.api.TextResponse
import org.adaptable.common.api.Request
import org.adaptable.common.web.WebRequest
import org.mockito.Mockito.mock
import java.util.concurrent.atomic.AtomicBoolean

class ResponseRuleImplTest : FunSpec({

    test("When true returns response") {
        val responseRule = ResponseRuleImpl(WebRequest::class.java) { it.body == "test" }
            .respondWith { TextResponse(200, "test") }

        (responseRule.process(WebRequest("test"), true)?.toDom() as org.adaptable.common.web.TextResponse)
            .body shouldBe "test"
    }

    test("When not true runs alternate rule") {
        val responseRule = ResponseRuleImpl(WebRequest::class.java) { it.body == "test" }
            .respondWith { TextResponse(200, "test") }
            .otherwise { it.body == "test2" }

        responseRule.respondWith { TextResponse(200, "test2") }

        (responseRule.process(WebRequest("test2"), true)?.toDom() as org.adaptable.common.web.TextResponse)
            .body shouldBe "test2"
    }

    test("When not to evaluate returns null") {
        val isTrue = AtomicBoolean()
        val responseRule = ResponseRuleImpl(WebRequest::class.java) { it.body == "test" }
            .respondWith { TextResponse(200, "test") }
            .perform { isTrue.set(true) }

        responseRule.otherwise(){ it.body == "test2" }
                .respondWith { TextResponse(200, "test2") }

        responseRule.process(WebRequest("test2"), false) shouldBe null
        isTrue.get() shouldBe false
    }

    test("Performs action") {
        val isTrue = AtomicBoolean()
        val responseRule = ResponseRuleImpl(WebRequest::class.java) { it.body == "test" }
            .respondWith { TextResponse(200, "test") }
            .perform { isTrue.set(true) }

        responseRule.otherwise() { it.body == "test2" }
            .respondWith { TextResponse(200, "test2") }

        responseRule.process(WebRequest("test"), false) shouldBe null
        isTrue.get() shouldBe true
    }

    test("When not correct type request is ignored") {
        val request = mock(Request::class.java)
        val isTrue = AtomicBoolean()
        val responseRule = ResponseRuleImpl(WebRequest::class.java) { it.body == "test" }
            .respondWith { TextResponse(200, "test") }
            .perform { isTrue.set(true) }

        responseRule.otherwise() { it.body == "test2" }
                .respondWith { TextResponse(200, "test2") }

        responseRule.process(request, false) shouldBe null
        isTrue.get() shouldBe false
    }
})
