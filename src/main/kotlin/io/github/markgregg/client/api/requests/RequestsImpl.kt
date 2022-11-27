package io.github.markgregg.client.api.requests

import io.reactivex.rxjava3.subjects.PublishSubject
import io.github.markgregg.client.api.Response
import io.github.markgregg.client.api.exceptions.TimeOutException
import io.github.markgregg.common.api.Request
import io.github.markgregg.expression.Evaluator
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class RequestsImpl internal constructor() : Requests {
    companion object {
        private val logger = LoggerFactory.getLogger(RequestsImpl::class.java)
    }
    private val defaultTimeout = 120000L
    private val requestSubject = PublishSubject.create<Request>()
    private val requests: MutableList<Request> = CopyOnWriteArrayList()
    private val rules: MutableList<ResponseRule<*>> = ArrayList()

    override fun add(request: Request): Response? {
        requests.add(request)
        requestSubject.onNext(request)
        var response: Response? = null
        rules.forEach {
            val ruleResponse = it.process(request, response == null)
            if( response == null && ruleResponse != null ) {
                response = ruleResponse
            }
        }
        return response
    }

    override fun requests(): List<Request> {
        return requests
    }

    override fun <T : Request> response(ofType: Class<T>, forCondition: (T) -> Boolean): ResponseRule<T> {
        val rule = ResponseRuleImpl(ofType, forCondition)
        rules.add(rule)
        return rule
    }

    override fun response(expression: String): ResponseRule<Request> {
        val rule = ResponseRuleImpl(Request::class.java, Evaluator.instance().compile(expression))
        rules.add(rule)
        return rule
    }

    override fun <T : Request> waitFor(ofType: Class<T>, forCondition: (T) -> Boolean, timeInMilliseconds: Long?): T {
        logger.debug("Waiting for request")
        return requests.filterIsInstance(ofType)
            .firstOrNull { forCondition(it) } ?: waitForNewRequest(ofType, forCondition, timeInMilliseconds ?: defaultTimeout )
    }

    override fun waitFor(expression: String, timeInMilliseconds: Long?): Request {
        logger.debug("Waiting for request")
        val operation = Evaluator.instance().compile(expression)
        return requests.firstOrNull { operation.execute(it) as Boolean }
            ?: waitForNewRequest( Request::class.java, { operation.execute(it) as Boolean }, timeInMilliseconds ?: defaultTimeout )
    }

    override fun hasResponses(): Boolean {
        return rules.any { it.hasResponse() }
    }

    private fun <T : Request> waitForNewRequest(clazz: Class<T>, predicate: (T) -> Boolean, timeout: Long): T {
        val response = AtomicReference<T>()
        val countDown = CountDownLatch(1)
        val disposable = requestSubject
            .ofType(clazz)
            .filter { predicate.invoke(clazz.cast(it)) }
            .subscribe {
                response.set(clazz.cast(it))
                countDown.countDown()
            }
        try {
            countDown.await(timeout, TimeUnit.MILLISECONDS)
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
}

