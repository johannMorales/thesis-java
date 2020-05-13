#!/bin/bash

cd "${0%/*}" && mvn package && java -jar ./target/thesis-connector-0.0.1-SNAPSHOT.jar