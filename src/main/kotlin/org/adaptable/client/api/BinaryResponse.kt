package org.adaptable.client.api

import org.springframework.http.HttpStatus
import java.nio.ByteBuffer
import java.util.*

class BinaryResponse(
    status: Int,
    private val body: ByteBuffer,
    headers: Map<String,String>
) : WebResponse(status, HashMap(headers)) {

    constructor(status: Int, body: ByteBuffer) : this(status, body, emptyMap() )

    constructor(body: ByteBuffer) : this(HttpStatus.OK.value(), body )

    override fun toDom(): org.adaptable.common.api.Response {
        return org.adaptable.common.web.BinaryResponse(status, Base64.getEncoder().encodeToString(body.array()), headers, false)
    }
}