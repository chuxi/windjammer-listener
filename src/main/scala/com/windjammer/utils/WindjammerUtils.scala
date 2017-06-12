package com.windjammer.utils

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.spark.SparkConf

/**
  * Created by king on 17-6-9.
  */
object WindjammerUtils {
  lazy val mapper: ObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .setSerializationInclusion(Include.NON_NULL)

  def reporterProperties(conf: SparkConf): Map[String, String] = {
    val len = "spark.windjammer.reporter.".length
    conf.getAll
      .filter(_._1.startsWith("spark.windjammer.reporter."))
      .map(kv => kv._1.substring(len) -> kv._2).toMap
  }

}
