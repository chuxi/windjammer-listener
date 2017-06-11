package com.windjammer.reporters

import com.windjammer.models.ApplicationInfo

/**
  * Created by king on 17-6-9.
  */
class InfluxDBReporter(conf: Map[String, String]) extends Reporter {
  /**
    * report content, dimensions and metrics ( always be long type )
    */
  override def report(info: ApplicationInfo): Unit = {



  }
}
