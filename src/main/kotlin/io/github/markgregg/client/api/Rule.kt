package io.github.markgregg.client.api

import io.github.markgregg.common.api.Rule

abstract class Rule {
    internal abstract fun toDom(): Rule
}