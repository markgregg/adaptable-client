package org.adaptable.client.api

import org.adaptable.common.api.Rule

abstract class Rule {
    internal abstract fun toDom(): Rule
}