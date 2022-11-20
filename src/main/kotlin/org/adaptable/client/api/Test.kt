package org.adaptable.client.api

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.adaptable.client.api.exceptions.*
import org.adaptable.client.api.requests.RequestsImpl
import org.adaptable.client.socket.WebSocketClientFactory
import org.adaptable.client.socket.WebSocketClientFactoryImpl
import org.adaptable.common.api.socket.AgentUnavailableException
import org.adaptable.common.api.socket.WebSocketClient
import org.adaptable.common.protocol.*
import org.adaptable.common.protocol.Response
import org.adaptable.expression.Evaluator
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


/***
 *
 * When test goes out of scope terminate connection and end test
 * n server if client is closed - end test case
 */
class Test internal constructor(
    private val websocketClientFactory: WebSocketClientFactory,
    private val responseTimeoutInMilliseconds: Int,
    subjectCreator: () -> Subject<Response>
) {
    constructor() : this(  WebSocketClientFactoryImpl(), 10000, { PublishSubject.create<Response>() })
    companion object {
        private val logger = LoggerFactory.getLogger(Test::class.java)
    }
    private val socketClient = AtomicReference<WebSocketClient?>()
    private val endPoints = HashMap<String, EndPoint>()
    private val id = UUID.randomUUID().toString()
    private val responseSubject = subjectCreator.invoke()
    private var responseDisposable: Disposable? = null

    init {
        logger.info("Initialising")
        Evaluator.instance()
    }

    /***
     *
     */
    @Throws(AgentUnavailableException::class)
    fun start(): Test {
        if( socketClient.get() != null ) {
            throw TestStartedException()
        }

        logger.info("Starting test")
        logger.info("Creating socket")
        val socket = websocketClientFactory.createSocketClient()
        try {
            logger.info("Connecting to agent")
            socket.connect()
            socket.addMessageHandler(responseSubject::onNext)
            socketClient.set(socket)
        } catch (e: AgentUnavailableException) {
            logger.error("Failed to connect to agent", e)
            throw e
        }

        logger.info("Listening for requests")
        responseDisposable = responseSubject
            .ofType(RequestResponse::class.java)
            .subscribe( this::addRequestToEndPoint )

        logger.info("Sending start test")
        val response = sendMessageAndWaitForResponse(StartTest(toDom()), StartTestResponse::class.java)
        if( !response.success) {
            socket.removeMessageHandler()
            socketClient.set(null)
            throw FailedToStartTestException(response.message ?: "Failed to start test")
        }
        return this
    }

    /***
     *
     */
    fun end(): Test {
        if( socketClient.get() != null ) {
            logger.info("Ending test")
            if( responseDisposable?.isDisposed == false) {
                responseDisposable?.dispose()
            }
            responseDisposable = null
            val response = sendMessageAndWaitForResponse(EndTest(id), EndTestResponse::class.java)
            if (!response.success) {
                logger.error("Failed to end test, reason: ${response.message}")
            }
            socketClient.get()!!.removeMessageHandler()
            socketClient.get()!!.close()
            socketClient.set(null)
        }
        return this
    }

    /***
     *
     */
    fun addEndPoint(id: String): EndPoint {
        val endPoint = EndPoint(id, this.EndPointController(), RequestsImpl())
        endPoints[endPoint.id] = endPoint
        return endPoint
    }

    /***
     *
     */
    fun endPoint(id: String): EndPoint? {
        return endPoints[id]
    }

    /***
     *
     */
    fun endPoints(): Collection<EndPoint> {
        return endPoints.values
    }

    private fun addRequestToEndPoint(requestResponse: RequestResponse) {
        logger.debug("Received end point request $requestResponse")
        val response = endPoint(requestResponse.endPointId)?.addRequest(requestResponse.request)
        if (response != null) {
            socketClient.get()!!.sendMessage(EndpointResponse(requestResponse.endPointId, response.toDom()))
        }
    }

    private fun toDom(): org.adaptable.common.api.Test {
        return org.adaptable.common.api.Test(id, endPoints.values.map { it.toDom() })
    }

    private fun <T, R : Response>sendMessageAndWaitForResponse(message: T, responseClazz: Class<R>): Response {
        val response = AtomicReference<Response>()
        val countDown = CountDownLatch(1)
        val disposable = responseSubject
            .filter { responseClazz.isInstance(it) || it is ErrorResponse }
            .subscribe {
                response.set(it)
                countDown.countDown()
            }
        try {
            socketClient.get()!!.sendMessage(message)
            countDown.await(responseTimeoutInMilliseconds.toLong(), TimeUnit.MILLISECONDS)
            if( response.get() == null) {
                throw TimeOutException()
            }
        } finally {
            if( !disposable.isDisposed ) {
                disposable.dispose()
            }
        }
        return response.get()
    }

    internal inner class EndPointController : Parent {
        override fun makeEndPointUnavailable(endPoint: String) {
            if( socketClient.get() == null ) {
                throw TestNotStartedException()
            }
            logger.info("Making end point unavailable $endPoint")
            val response = sendMessageAndWaitForResponse(MakeUnavailable(endPoint), MakeUnavailableResponse::class.java)
            if ( !response.success ) {
                throw FailedToMakeEndPointUnavailableException(response.message ?: "Failed to make endpoint $endPoint unavailable")
            }
        }

        override fun makeEndPointAvailable(endPoint: String) {
            if( socketClient.get() == null ) {
                throw TestNotStartedException()
            }
            logger.info("Making end point available $endPoint")
            val response = sendMessageAndWaitForResponse(MakeAvailable(endPoint), MakeAvailableResponse::class.java)
            if ( !response.success ) {
                throw FailedToMakeEndPointUnavailableException(response.message ?: "Failed to make endpoint $endPoint unavailable")
            }
        }

        override fun sendMessage(message: Message<*>) {
            if( socketClient.get() == null ) {
                throw TestNotStartedException()
            }
            logger.info("Sending ($message)")
            val msgResponse = sendMessageAndWaitForResponse(message, MessageResponse::class.java)
            if ( !msgResponse.success ) {
                throw SendMessageException(msgResponse.message ?: "Failed to send $message")
            }
        }
    }
}
