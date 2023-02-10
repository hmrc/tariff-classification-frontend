#!/usr/bin/env bash

sbt clean compile scalafmtAll scalastyleAll coverage Test/test IntegrationTest/test dependencyUpdates coverageReport

