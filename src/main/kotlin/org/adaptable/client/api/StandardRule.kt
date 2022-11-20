package org.adaptable.client.api

import org.adaptable.common.api.StandardRule

class StandardRule(
    private var expression: String?,
    private var response: Response?,
    private var responses: List<Response>?,
    private var otherwise: Rule?
) : Rule() {
    constructor(expression: String, response: Response) : this(expression,  response, null, null)
    constructor(expression: String,responses: List<Response>) : this(expression,null, responses, null)
    constructor(expression: String, response: Response, otherwise: Rule) : this(expression,  response, null, otherwise)
    constructor(expression: String,responses: List<Response>, otherwise: Rule) : this(expression,null, responses, otherwise)

    override fun toDom(): org.adaptable.common.api.Rule {
        return StandardRule(
            expression,
            response?.toDom(),
            responses?.map{ it.toDom() },
            otherwise?.toDom())
    }
}