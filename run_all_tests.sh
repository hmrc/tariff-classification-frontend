#!/usr/bin/env bash
sbt -mem 2048 clean compile coverage test it:test coverageOff coverageReport
