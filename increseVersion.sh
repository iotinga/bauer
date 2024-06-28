#!/bin/bash
mvn -B release:prepare release:perform
mvn deploy
