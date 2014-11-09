#!/bin/sh
rm -rf lecarton-1.0-SNAPSHOT/
./activator dist
unzip target/universal/lecarton-1.0-SNAPSHOT.zip
lecarton-1.0-SNAPSHOT/bin/lecarton -Dhttp.port=$1
