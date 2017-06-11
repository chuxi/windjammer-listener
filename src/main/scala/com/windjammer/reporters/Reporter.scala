package com.windjammer.reporters

import com.windjammer.models.ApplicationInfo

/**
  * Created by king on 17-6-9.
  */
trait Reporter {
  /**
    * generate spark application running report
    */
  def report(info: ApplicationInfo): Unit
}
