#!/usr/bin/env bash

sbt -J-Xmx4G clean scalafmtAll compile coverage test it/test dependencyUpdates coverageReport
