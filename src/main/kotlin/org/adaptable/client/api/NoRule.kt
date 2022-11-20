package org.adaptable.client.api

import org.adaptable.common.api.NoRule

class NoRule(
    private var response: Response?,
    private var responses: List<Response>?
) : Rule() {
    constructor(response: Response) : this(response, null)
    constructor(responses: List<Response>) : this(null, responses)

    override fun toDom(): org.adaptable.common.api.Rule {
        return NoRule(
            response?.toDom(),
            responses?.map{ it.toDom()} )
    }
}