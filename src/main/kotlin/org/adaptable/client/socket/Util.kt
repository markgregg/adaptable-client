package org.adaptable.client.socket

import io.github.classgraph.AnnotationParameterValueList
import io.github.classgraph.ClassGraph
import org.adaptable.client.api.Test
import org.adaptable.client.api.exceptions.ClientInitializerNotFoundException
import org.slf4j.LoggerFactory
import java.net.URI


internal object Util {
   private val logger = LoggerFactory.getLogger(Test::class.java)
   private val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
   internal var namespaces = emptyArray<String>()
   private var uri: URI? = null
   private var delayBetweenAttemptsToConnect: Long? = null
   private var attemptsToConnect: Short? = null
   private var annotationParameterValueList: AnnotationParameterValueList? = null

   fun agentURI(): URI {
      if( uri == null ) {
         val params = annotationParameterValueList ?: getParameterList()
         val hostPort = params.getValue("value").toString().split(":")
         val port = if( hostPort.size < 2 || !hostPort[1].isNumeric()) {
            8080
         } else {
            hostPort[1].toInt()
         }
         uri = URI("ws://${hostPort[0]}:$port/agentClient/api")
      }
      logger.debug("uri($uri)")
      return uri!!
   }

   fun delayBetweenAttemptsToConnect(): Long {
      if( delayBetweenAttemptsToConnect == null ) {
         val params = annotationParameterValueList ?: getParameterList()
         delayBetweenAttemptsToConnect = params.getValue("delayBetweenAttemptsToConnect").toString().toLong()
      }
      logger.debug("delayBetweenAttemptsToConnect($delayBetweenAttemptsToConnect)")
      return delayBetweenAttemptsToConnect!!
   }

   fun attemptsToConnect(): Short {
      if( attemptsToConnect == null ) {
         val params = annotationParameterValueList ?: getParameterList()
         attemptsToConnect = params.getValue("attemptsToConnect").toString().toShort()
      }
      logger.debug("attemptsToConnect($attemptsToConnect)")
      return attemptsToConnect!!
   }

   private fun getParameterList(): AnnotationParameterValueList {
      ClassGraph()
         .enableClassInfo()
         .enableExternalClasses()
         .ignoreClassVisibility()
         .enableAnnotationInfo()
         .acceptPackages(*namespaces)
         .scan().use { result ->
            annotationParameterValueList = result.getClassesWithAnnotation(ClientInitializer::class.java)
               .map { it.getAnnotationInfo(ClientInitializer::class.java)?.parameterValues }
               .firstOrNull() ?: throw ClientInitializerNotFoundException()
         }

      return annotationParameterValueList!!
   }

   private fun String.isNumeric(): Boolean {

      return this.matches(regex)
   }
}