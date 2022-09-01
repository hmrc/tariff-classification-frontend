#!/usr/bin/env bash
sbt -mem 2048 clean scalafmtAll scalastyle compile coverage test it:test coverageOff coverageReport dependencyUpdates

