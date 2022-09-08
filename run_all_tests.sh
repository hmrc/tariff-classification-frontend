#!/usr/bin/env bash
sbt -mem 2048 clean scalafmtAll scalastyleAll compile coverage test it:test coverageOff coverageReport dependencyUpdates

