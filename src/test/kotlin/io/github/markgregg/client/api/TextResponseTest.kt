package io.github.markgregg.client.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TextResponseTest : FunSpec({

    test("toDom") {
        val domResponse = TextResponse(200, "hello").toDom() as io.github.markgregg.common.web.TextResponse

        domResponse.status shouldBe 200
        domResponse.payload() shouldBe "hello"
        domResponse.headers shouldBe emptyMap()
    }
})
