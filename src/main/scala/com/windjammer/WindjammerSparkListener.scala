package com.windjammer

import java.util

import com.windjammer.models._
import com.windjammer.reporters.Reporter
import com.windjammer.utils.WindjammerUtils
import org.apache.spark.SparkConf
import org.apache.spark.scheduler._
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

/**
  * Created by king on 17-6-8.
  */
class WindjammerSparkListener(conf: SparkConf) extends SparkListener {
  private val logger = LoggerFactory.getLogger(getClass)
  private val reporter: Reporter = createReporter()

  private var name: String = _
  private var appId: String = _
  private var appStartTime: Long = _
  private var appEndTime: Long = _

  // jobId -> (jobStartTime, jobEndTime, stageIds)
  private val jobs: util.HashMap[Int, (Long, Long, Seq[Int])] =
    new util.HashMap[Int, (Long, Long, Seq[Int])]()

  // stageId -> (submissionTime, stageEndTime, stageRDD which runs on
  private val stages: util.HashMap[Int, (Long, Long, Seq[RDDInfo])] =
    new util.HashMap[Int, (Long, Long, Seq[RDDInfo])]()

  private val stageMapToTasks: util.HashMap[Int, ArrayBuffer[Long]] =
    new util.HashMap[Int, ArrayBuffer[Long]]()

  private val tasks: util.HashMap[Long, (Long, Long)] = new util.HashMap[Long, (Long, Long)]()


  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {
    name = applicationStart.appName
    if (applicationStart.appId.isDefined) {
      appId = applicationStart.appId.get
    }
    appStartTime = applicationStart.time
  }

  // statistic job running time
  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    jobs.put(jobStart.jobId, (jobStart.time, 0L, jobStart.stageIds))
  }

  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    val taskId = taskEnd.taskInfo.taskId
    tasks.put(taskId, (taskEnd.taskInfo.launchTime, taskEnd.taskInfo.finishTime))

    if (stageMapToTasks.containsKey(taskEnd.stageId)) {
      stageMapToTasks.get(taskEnd.stageId).append(taskEnd.taskInfo.taskId)
    } else {
      stageMapToTasks.put(taskEnd.stageId, ArrayBuffer(taskEnd.taskInfo.taskId))
    }
  }

  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = {
    val info = stageCompleted.stageInfo
    val rddInfos = info.rddInfos.map(r =>
      RDDInfo(r.numCachedPartitions, r.numPartitions, r.memSize, r.diskSize))

    stages.put(info.stageId, (info.submissionTime.get, info.completionTime.get, rddInfos))
  }

  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    if (jobs.containsKey(jobEnd.jobId)) {
      val value = jobs.get(jobEnd.jobId).copy(_2 = jobEnd.time)
      jobs.put(jobEnd.jobId, value)
    } else {
      logger.error(s"missing job id (${jobEnd.jobId}) in jobs.")
    }
  }

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
    appEndTime = applicationEnd.time

    reporter.report(buildApplicationInfo())
  }

  private def createReporter(): Reporter = {
    val props = WindjammerUtils.reporterProperties(conf)
    val className = props.getOrElse("class",
      throw new Exception("spark.windjammer.reporter.class is not defined."))
    val reporter = Class.forName(className)
      .getConstructor(classOf[Map[String, String]])
      .newInstance(props)
    reporter.asInstanceOf[Reporter]
  }

  private def buildApplicationInfo(): ApplicationInfo = {
    val jobInfo = jobs.asScala.map { job =>
      val stageInfo = job._2._3.filter(stages.containsKey(_))
        .map { stageId =>
          val stage = stages.get(stageId)
          val taskInfo = stageMapToTasks.get(stage)
            .filter(tasks.containsKey(_))
            .map { taskId =>
              val task = tasks.get(taskId)
              TaskInfo(task._1, task._2)
            }

          StageInfo(stageId, stage._1, stage._2, taskInfo, stage._3)
        }

      JobInfo(job._1, job._2._1, job._2._2, stageInfo)
    }.toSeq

    val statisticJobs = jobInfo.map { job =>
      val jobRunningTime = job.jobEndTime - job.jobStartTime
      val statisticStages = job.stages.map { stage =>

        val stageRunningTime = stage.completionTime - stage.submissionTime
        val tasksTimeCost = stage.tasks.map(task => task.finishTime - task.launchTime).sum
        val timeCompressedRatio = tasksTimeCost * 1.0 / stageRunningTime
        val totalDiskSize = stage.rdd.map(r => r.diskSize).sum
        val missCachedSize = if (totalDiskSize > 0) Some(totalDiskSize) else None

        StatisticStageInfo(stage.stageId,
          stageRunningTime,
          tasksTimeCost,
          timeCompressedRatio,
          missCachedSize)
      }

      val stagesTimeCost = statisticStages.map(_.stageRunningTime).sum

      StatisticJobInfo(job.jobId, jobRunningTime, stagesTimeCost, statisticStages)
    }

    val appRunningTime = appEndTime - appStartTime
    val jobsTimeCost = statisticJobs.map(_.jobRunningTime).sum

    val statisticInfo = StatisticInfo(appRunningTime, jobsTimeCost, statisticJobs)

    ApplicationInfo(name, appId, appStartTime,
      appEndTime, jobInfo, statisticInfo)
  }

}
