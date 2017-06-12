package com.windjammer.models

/**
  * Created by king on 17-6-10.
  */
case class ApplicationInfo(name: String,
                           appId: String,
                           appStartTime: Long,
                           appEndTime: Long,
                           jobs: Seq[JobInfo],
                           statistics: StatisticInfo)

case class JobInfo(jobId: Int,
                   jobStartTime: Long,
                   jobEndTime: Long,
                   stages: Seq[StageInfo])

case class StageInfo(stageId: Int,
                     submissionTime: Long,
                     completionTime: Long,
                     tasks: Seq[TaskInfo],
                     rdd: RDDInfo)

case class RDDInfo(numCachedPartitions: Int,
                   totalPartitions: Int,
                   memorySize: Long,
                   diskSize: Long)

case class TaskInfo(launchTime: Long, finishTime: Long)