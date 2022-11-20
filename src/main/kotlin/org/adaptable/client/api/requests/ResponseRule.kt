package org.adaptable.client.api.requests


import org.adaptable.client.api.Response
import org.adaptable.common.api.Request

interface ResponseRule<T : Request> {
    fun respondWith( response: (T) -> Response) : ResponseRule<T>
    fun otherwise( forCondition: (T) -> Boolean ) : ResponseRule<T>
    fun otherwise( expression: String ) : ResponseRule<Request>
    fun perform( action: (T) -> Unit) : ResponseRule<T>
    fun process(request: Request, evaluate: Boolean) : Response?
    fun hasResponse(): Boolean
}