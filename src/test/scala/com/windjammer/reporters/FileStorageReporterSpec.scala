package com.windjammer.reporters

import java.io.FileInputStream

import com.windjammer.models.{ApplicationInfo, StatisticInfo}
import com.windjammer.utils.WindjammerUtils
import org.scalatest.FunSuite

/**
  * Created by king on 17-6-11.
  */
class FileStorageReporterSpec extends FunSuite {

  test("fill reporter should store the report correctly") {
    val reporter = new FileStorageReporter(Map("store" -> "/tmp"))
    val info = ApplicationInfo("test", "123", 1, 20, Seq(),
      StatisticInfo(20, 30, Seq()))
    reporter.report(info)

    // read the file
    val in = new FileInputStream("/tmp/test-123")
    val storedInfo = WindjammerUtils.mapper.readValue(in, classOf[ApplicationInfo])
    assert(storedInfo equals info)
  }

}
