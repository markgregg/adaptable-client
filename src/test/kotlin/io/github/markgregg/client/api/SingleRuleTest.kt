package io.github.markgregg.client.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.github.markgregg.common.web.WebRequest
import java.util.*

class SingleRuleTest : FunSpec({

    test("toDom single response") {
        val response = TextResponse(200, "hello")
        val ruleDom = StandardRule(
            "\$body.test=='value'",
            response
        ).toDom()

        (ruleDom.evaluate(
            WebRequest(
                UUID.randomUUID(),
                emptyMap(),
                emptyMap(),
                "{\"test\": \"value\"}"
            )
        ) as io.github.markgregg.common.web.TextResponse).payload() shouldBe "hello"
    }

    test("toDom multiple responses") {

        val responses = listOf(TextResponse(200, "hello"))
        val ruleDom = StandardRule(
            "\$body.test=='value'",
            responses
        ).toDom()

        (ruleDom.evaluate(
            WebRequest(
                UUID.randomUUID(),
                emptyMap(),
                emptyMap(),
                "{\"test\": \"value\"}"
            )
        ) as io.github.markgregg.common.web.TextResponse).payload() shouldBe "hello"
    }
})
