package com.windjammer.models

/**
  * Created by king on 17-6-11.
  */
case class StatisticInfo(appRunningTime: Long,
                         jobsTimeCost: Long,
                         jobs: Seq[StatisticJobInfo])

case class StatisticJobInfo(jobId: Int,
                            jobRunningTime: Long,
                            stagesTimeCost: Long,
                            stages: Seq[StatisticStageInfo])

case class StatisticStageInfo(stageId: Int,
                              stageRunningTime: Long,
                              tasksTimeCost: Long,
                              timeCompressedRatio: Double,
                              missCachedSize: Option[Long])