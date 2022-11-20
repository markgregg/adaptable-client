package org.adaptable.client.api

import org.adaptable.common.protocol.Message


internal interface Parent {
    fun makeEndPointUnavailable(endPoint: String)
    fun makeEndPointAvailable(endPoint: String)
    fun sendMessage(message: Message<*>)
}
