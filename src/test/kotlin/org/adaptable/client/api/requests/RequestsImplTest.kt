package org.adaptable.client.api.requests

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.adaptable.client.api.TextResponse
import org.adaptable.client.api.exceptions.TimeOutException
import org.adaptable.common.web.WebRequest
import java.util.*
import kotlin.concurrent.timerTask

class RequestsImplTest : FunSpec() {

    init {

        test("add") {
            val request = RequestsImpl()

            request.response(WebRequest::class.java) { it.body == "test" }
                .respondWith { TextResponse(200, "test") }

            (request.add(WebRequest("test"))?.toDom() as org.adaptable.common.web.TextResponse)
                .body shouldBe "test"
        }

        test("only the response of the first rule is produced") {
            val request = RequestsImpl()

            request.response(WebRequest::class.java) { it.body == "test" }
                .respondWith { TextResponse(200, "test") }
            request.response(WebRequest::class.java) { it.body == "test" }
                .respondWith { TextResponse(200, "test2") }

            (request.add(WebRequest("test"))?.toDom() as org.adaptable.common.web.TextResponse)
                .body shouldBe "test"
        }

        test("requests") {
            val request = RequestsImpl()

            request.add(WebRequest("test"))
            request.add(WebRequest("test"))

            request.requests().size shouldBe 2
        }

        test("waitForRequest using an expression that has already been add") {
            val request = RequestsImpl()

            request.add(WebRequest("test"))

            (request.waitFor("\$text==\'test\'", 2000) as WebRequest).body shouldBe "test"
        }

        test("waitForRequest using an expression that has yet to be add") {
            val request = RequestsImpl()

            val timer = Timer("test", false)
            timer.schedule(timerTask { request.add(WebRequest("test")) }, 500)

            (request.waitFor("\$text==\'test\'", 2000) as WebRequest).body shouldBe "test"
            timer.cancel()
            timer.purge()
        }

        test("waitForRequest that has already been add") {
            val request = RequestsImpl()

            request.add(WebRequest("test"))
            request.waitFor(WebRequest::class.java, { it.body == "test" }, 2000).body shouldBe "test"
        }

        test("waitForRequest that has yet to be add") {
            val request = RequestsImpl()

            val timer = Timer("test", false)
            timer.schedule(timerTask { request.add(WebRequest("test")) }, 500)

            request.waitFor(WebRequest::class.java, { it.body == "test" }, 5000).body shouldBe "test"
            timer.cancel()
            timer.purge()
        }

        test("waitForRequest times out in specified time") {
            val request = RequestsImpl()

            shouldThrow<TimeOutException> {
                request.waitFor(WebRequest::class.java, { it.body == "test" }, 1000)
            }
        }
    }
}
