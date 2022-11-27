package io.github.markgregg.client.api

import io.github.markgregg.common.api.NoRule

class NoRule(
    private var response: Response?,
    private var responses: List<Response>?
) : Rule() {
    constructor(response: Response) : this(response, null)
    constructor(responses: List<Response>) : this(null, responses)

    override fun toDom(): io.github.markgregg.common.api.Rule {
        return NoRule(
            response?.toDom(),
            responses?.map{ it.toDom()} )
    }
}