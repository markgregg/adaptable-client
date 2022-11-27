package io.github.markgregg.client.api.requests


import io.github.markgregg.client.api.Response
import io.github.markgregg.common.api.Request
import io.github.markgregg.expression.Evaluator
import io.github.markgregg.expression.operations.Operation

class ResponseRuleImpl<T : Request> internal constructor(
    private val ofType: Class<T>,
    private val condition: (T) -> Boolean
) : ResponseRule<T> {

    internal constructor(ofType: Class<T>, operation: Operation) : this(ofType, { r -> operation.execute(r) as Boolean })

    private var otherwise: ResponseRule<*>? = null
    private var response: ((T) -> Response)? = null
    private var action: ((T) -> Unit)? = null

    override fun respondWith(response: (T) -> Response): ResponseRule<T> {
        this.response = response
        return this
    }

    override fun otherwise(forCondition: (T) -> Boolean): ResponseRule<T> {
        val responseRule =  ResponseRuleImpl(ofType, forCondition)
        this.otherwise = responseRule
        return responseRule
    }

    override fun otherwise( expression: String): ResponseRule<Request> {
        val responseRule = ResponseRuleImpl(Request::class.java, Evaluator.instance().compile(expression))
        this.otherwise = responseRule
        return responseRule
    }

    override fun perform(action: (T) -> Unit): ResponseRule<T> {
        this.action = action
        return this
    }

    override fun process(request: Request, evaluate: Boolean): Response? {
        if(!ofType.isAssignableFrom(request.javaClass)) {
            return null
        }
        val typeRequest = ofType.cast(request)
        return if( typeRequest == null ) {
            null
        } else if (condition.invoke( typeRequest )) {
            action?.invoke(typeRequest)
            if( evaluate ) {
                response?.invoke( typeRequest )
            } else {
                null
            }
        } else {
            otherwise?.process(request, evaluate)
        }
    }

    override fun hasResponse(): Boolean {
        return response != null
    }
}