# Serverless Manifest Service

This repo is based on an example written by [@bahern](https://github.com/bahern).

## Overview
Simple video transcoding pipeline and streaming service build on top of Kolin, Spring boot, AWS Lambda, AWS Elastic Transcoder, API Gateway, and more.

![Architectural Diagram](img/architecture.png)

## Requirements
- Serverless Framework
- Java 11
- [Native HLS](https://addons.mozilla.org/en-US/firefox/addon/native_hls_playback/), If using Firefox or Chrome.
  
## Setup

### Create the Elastic Transcoder pipeline
This setup has a single manual step, create the Elastic Transcoder pipeline, once created, take note of the `pipeline-id`.

### Compile project 
Using the gradle task `shadowJar` compile and create the packages
```bash
./gradlew shadowJar
```

### Deploy the CloudFormation stack
Using the serverless framework and the `pipeline-id` from the first step.
```bash
serverless deploy --pipelineId <PIPELINE_ID>
```
### Update the Elastic Transcoder pipeline buckets
Update the Elastic Transcoder input and output buckets with the values of the CloudFormation outputs `InputBucketName` 
and `OutputBucketName`

## How to use

1. Upload a video file e.g. `example.mp4` to the input bucket.
1. The pipeline will produce the following files:
    - hls400k_frames.json
    - hls400k.json
    - hls400k.ts
    - hls600k_frames.json
    - hls600k.json
    - hls1000k_frames.json	
    - hls1000k.ts
    - hls1500k_frames.json
    - hls1500k.json
    - hls1500k.ts
    - hls2000k_frames.json
    - hls2000k.json
    - hls2000k.ts
1. Once these files have been created, visit the API Gateway endpoint in Safari (or using the [Native HLS](https://addons.mozilla.org/en-US/firefox/addon/native_hls_playback/)) plugin:
```
https://<ID>.execute-api.us-<REGION>.amazonaws.com/<STAGE>/video/example/master.m3u8
```