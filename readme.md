## Goal

Windjammer Spark Listeners collect and store spark application run time details for statistics.

## data structure

```
{
  name: "appName",
  appId: "appId",
  appStartTime: ...,
  appEndTime: ...,
  jobs: [
    {
      jobId: 0
      jobStartTime: 1497098247   # milliseconds
      jobEndTime: 1497198247

      stages: [
        {
          stageId: 0
          submissionTime: 1497098247
          completionTime: 1497198247
          tasks: [
            {
              launchTime: 1497098247
              finishTime: 1497098247
            }, {
              ...
            }
          ],
          rdd: [
            {
              numCachedPartitions: 4,
              totalPartitions: 8,
              memorySize: "20MB",
              diskSize: "500MB"
            }, {
              ...
            }
          ]
        }, {
          ...
        }
      ]
    }, {
      jobId: 1
      ...
    }
  ]
  statistics: {
    appRunningTime: ${appEndTime - appStartTime},
    jobsTimeCost: ${add all jobs time together},
    jobs: [
      {
        jobId: 0
        jobRunningTime: ${jobEndTime - jobStartTime},
        stagesTimeCost: ...
        stages: [
          {
            stageRunningTime: ${completionTime - submissionTime},
            tasksTimeCost: ...
            ratio: ${stageTaskRunningTime / stageRunningTime}
          }

        ]
      }
    ]
  }
}
```
