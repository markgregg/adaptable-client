package io.github.markgregg.client.api

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.github.markgregg.client.api.exceptions.FailedToStartTestException
import io.github.markgregg.client.api.exceptions.TestNotStartedException
import io.github.markgregg.client.api.exceptions.TestStartedException
import io.github.markgregg.client.api.exceptions.TimeOutException
import io.github.markgregg.client.socket.WebSocketClientFactory
import io.github.markgregg.common.api.socket.AgentUnavailableException
import io.github.markgregg.common.api.socket.WebSocketClient
import io.github.markgregg.common.protocol.*
import io.github.markgregg.common.protocol.Response
import org.mockito.Mockito.mock
import org.mockito.kotlin.*

class TestTest : FunSpec() {
    private var websocketClientFactory: WebSocketClientFactory? = null
    private var socket: WebSocketClient? = null

    override suspend fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        websocketClientFactory = mock(WebSocketClientFactory::class.java)
        socket = mock(WebSocketClient::class.java)
        whenever(websocketClientFactory!!.createSocketClient()).thenReturn(socket)
    }

    init {

        test("create") {
            Test().shouldNotBeNull()
        }

        test("when test starts message socket connects and message is sent") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.addEndPoint("id").addRule(
                StandardRule(
                    "\$body.field=='value'",
                    TextResponse(200,"test")
                )
            )
            test.start() shouldBe test
            verify(socket!!).connect()
            verify(socket!!).addMessageHandler(isA())
            val captor = argumentCaptor<StartTest>()
            verify(socket!!).sendMessage(captor.capture())
            captor.firstValue.test.endPoints.size shouldBe 1
            captor.firstValue.test.endPoints[0].rules?.size shouldBe 1
        }

        test("when start is called an an error occurs an exception is thrown") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }

            doAnswer {
                throw AgentUnavailableException()
            }.whenever(socket!!).connect()
            shouldThrowExactly<AgentUnavailableException> {
                test.start()
            }

            verify(socket!!, times(0)).addMessageHandler(isA())
            verify(socket!!, times(0)).sendMessage(any<StartTest>())
        }

        test("when test fails to start ensure socket is clear occurs") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(false, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            val exception = shouldThrowExactly<FailedToStartTestException> {
                test.start()
            }
            verify(socket!!).connect()
            verify(socket!!).addMessageHandler(isA())
            verify(socket!!).sendMessage(any<StartTest>())
            verify(socket!!).removeMessageHandler()
            exception.message shouldBe "Failed to start test"
        }

        test("when test running already running exception thrown") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.start() shouldBe test

            shouldThrowExactly<TestStartedException> {
                test.start() shouldBe test
            }
        }

        test("when test ended message is sent and response received") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.start() shouldBe test
            behaviourSubject.onNext(EndTestResponse(true, null))
            test.end() shouldBe test

            verify(socket!!).close()
            verify(socket!!).removeMessageHandler()
            verify(socket!!).sendMessage(any<EndTest>())
        }

        test("when test not running end does nothing") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(EndTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.end() shouldBe test

            verify(socket!!, times(0)).sendMessage(any<EndTest>())
        }

        test("when end test fails it is handled") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.start() shouldBe test
            behaviourSubject.onNext(EndTestResponse(false, null))
            test.end() shouldBe test

            verify(socket!!).sendMessage(any<EndTest>())
            verify(socket!!).close()
            verify(socket!!).removeMessageHandler()
        }

        test("when an endpoint is add it can be retrieved") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.addEndPoint("id").id shouldBe "id"
            test.endPoints() shouldBe listOf(test.endPoint("id"))
        }


        test("when make test point unavailable called message is sent and response received") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.start() shouldBe test
            behaviourSubject.onNext(MakeUnavailableResponse(true, null))
            test.EndPointController().makeEndPointUnavailable("id")

            verify(socket!!).sendMessage(any<MakeUnavailable>())
        }

        test("if test not running then an exception is thrown") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            behaviourSubject.onNext(MakeUnavailableResponse(true, null))

            shouldThrowExactly<TestNotStartedException> {
                test.EndPointController().makeEndPointUnavailable("id")
            }
        }

        test("when make test point available called message is sent and response received") {
            val behaviourSubject = BehaviorSubject.create<Response>()
            behaviourSubject.onNext(StartTestResponse(true, null))
            val test = Test(websocketClientFactory!!, 1000) { behaviourSubject }
            test.start() shouldBe test
            behaviourSubject.onNext(MakeAvailableResponse(true, null))
            test.EndPointController().makeEndPointAvailable("id")

            verify(socket!!).sendMessage(any<MakeAvailable>())
        }


        test("when message not received timeout is thrown") {
            val test = Test(websocketClientFactory!!, 1000) {  PublishSubject.create() }
            shouldThrowExactly<TimeOutException> {
                test.start() shouldBe test
            }
        }
    }
}