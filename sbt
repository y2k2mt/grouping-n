#!/usr/bin/env bash

set -euo pipefail
IFS=$'\n\t'

WORKING_DIR=".sbt-local"

mkdir -p $WORKING_DIR
cd $WORKING_DIR

VERSION="1.6.2"
ARCHIVE="sbt-${VERSION}.zip"
EXTRACTED="sbt-${VERSION}"
URL="https://github.com/sbt/sbt/releases/download/v${VERSION}/${ARCHIVE}"

if [ ! -f $ARCHIVE ]
then
  echo "Downloading sbt"
  curl -LOk $URL
fi

if [ ! -d $EXTRACTED ]
then
  echo "Extracting sbt"
  unzip -q $ARCHIVE -d $EXTRACTED
fi

cd ..

NOW=$(date -u +"%Y-%m-%dT%H:%M:%Z")
echo "Starting sbt "$VERSION" at "$NOW
#JAVA_OPTS="-Xms1g -Xmx1g -XX:MaxMetaspaceSize=1g" SBT_OPTS="-Dsbt.override.build.repos=true -Dsbt.repository.config=./repositories" ./${WORKING_DIR}/${EXTRACTED}/sbt/bin/sbt $*
JAVA_OPTS="-Xms1g -Xmx1g -XX:MaxMetaspaceSize=1g" ./${WORKING_DIR}/${EXTRACTED}/sbt/bin/sbt $*
