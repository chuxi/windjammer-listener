package com.windjammer.reporters
import java.io.{BufferedOutputStream, File, FileOutputStream}

import com.windjammer.models.ApplicationInfo
import com.windjammer.utils.WindjammerUtils
import org.slf4j.LoggerFactory

/**
  * Created by king on 17-6-9.
  * store the application info into master local file.
  */
class FileStorageReporter(conf: Map[String, String]) extends Reporter {
  private val logger = LoggerFactory.getLogger(getClass)
  private val store: String = conf.getOrElse("store",
    throw new Exception("missing store for FileStorageReporter."))

  override def report(info: ApplicationInfo): Unit = {
    val dir = new File(store)
    if (!dir.exists()) dir.mkdirs()

    val filename = store + s"/${info.name}"
    logger.info(s"writing statistic result into $store")
    var file: FileOutputStream = null
    try {
      file = new FileOutputStream(filename)
      val data = WindjammerUtils.mapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsBytes(info)
      file.write(data)
      file.flush()
    } catch {
      case e: Throwable =>
        logger.error(s"can not write into file $store.", e)
    } finally {
      if (file != null) {
        file.close()
      }
    }
  }
}
