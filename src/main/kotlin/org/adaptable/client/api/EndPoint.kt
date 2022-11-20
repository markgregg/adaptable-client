package org.adaptable.client.api

import org.adaptable.client.api.requests.Requests
import org.adaptable.common.api.Request
import org.adaptable.common.protocol.Message
import org.slf4j.LoggerFactory

/**
 *
 */
class EndPoint internal constructor(
    internal val id: String,
    internal val parent: Parent?,
    private val requests: Requests
) {
    companion object {
        private val logger = LoggerFactory.getLogger(EndPoint::class.java)
    }
    private val rules = ArrayList<Rule>()
    private var unavailable: Boolean = false

    /***
     *
     */
    fun rules(): List<Rule> {
        return rules
    }

    /***
     *
     */
    fun addRule(rule: Rule): EndPoint {
        rules.add(rule)
        return this
    }

    /***
     *
     */
    fun requests(): Requests {
        return requests
    }

    /**
     *
     */
    fun makeUnavailable(): EndPoint {
        unavailable = true
        parent?.makeEndPointUnavailable(id)
        return this
    }

    /***
     *
     */
    fun makeAvailable(): EndPoint {
        unavailable = false
        parent?.makeEndPointAvailable(id)
        return this
    }

    fun sendMessage(response: Response) {
        parent?.sendMessage(Message(id, response.toDom()))
    }

    fun test(request: Request): org.adaptable.common.api.Response? {
        for( rule in rules ) {
            val response = rule.toDom().evaluate(request)
            if( response != null ) {
                return response
            }
        }
        return null
    }

    internal fun addRequest(request: Request): Response? {
        logger.debug("Adding request")
        return requests.add(request)
    }

    internal fun toDom() : org.adaptable.common.api.EndPoint {
        return org.adaptable.common.api.EndPoint(id, rules.map { it.toDom() }, unavailable, requests.hasResponses() )
    }

}