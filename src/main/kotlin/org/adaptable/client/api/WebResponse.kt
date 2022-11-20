package org.adaptable.client.api

import org.springframework.http.HttpHeaders

abstract class WebResponse(
    protected val status: Int,
    protected val headers: MutableMap<String,String>
) : Response() {

    fun addAccessControlAllowCredentials(allowCredentials: Boolean): WebResponse {
        headers[HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS] = allowCredentials.toString()
        return this
    }

    fun addAccessControlAllowHeaders(controlHeaders: List<String>): WebResponse {
        val allowHeaders =
            if( controlHeaders.isEmpty() ) {
                "*"
            } else {
                controlHeaders.joinToString()
            }
        headers[HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS] = allowHeaders
        return this
    }

    fun addAccessControlAllowMethods(methods: List<String>): WebResponse {
        val allowMethods =
            if( methods.isEmpty() ) {
                "*"
            } else {
                methods.joinToString()
            }
        headers[HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS] = allowMethods
        return this
    }

    fun addAccessControlAllowOrigin(origin: String): WebResponse {
        headers[HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN] = origin
        return this
    }

    fun addAccessControlExposeHeaders(headersToExpose: List<String>): WebResponse {
        val exposedMethods =
            if( headersToExpose.isEmpty() ) {
                "*"
            } else {
                headersToExpose.joinToString()
            }
        headers[HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS] = exposedMethods
        return this
    }
}