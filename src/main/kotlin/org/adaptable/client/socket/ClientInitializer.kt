package org.adaptable.client.socket

@Target(AnnotationTarget.CLASS)
annotation class ClientInitializer(
    val value: String,
    val attemptsToConnect: Short = 5,
    val delayBetweenAttemptsToConnect: Long = 10000L
)
