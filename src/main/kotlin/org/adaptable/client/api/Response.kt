package org.adaptable.client.api

import org.adaptable.common.api.Response

abstract class Response {
    internal abstract fun toDom(): Response
}