#!/usr/bin/env bash
sbt -mem 2048 clean scalastyle compile coverage test it:test coverageOff coverageReport

