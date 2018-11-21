#!/bin/bash
sudo apt-get update
sudo apt-get install -y openjdk-8-jre
export JAVA_HOME='/usr/lib/jvm/java-1.8.0-openjdk-amd64'

wget https://archive.apache.org/dist/kafka/1.1.0/kafka_2.12-1.1.0.tgz
tar -xvf kafka_2.12-1.1.0.tgz
mkdir tmp/kafka/logs -p
rm kafka_2.12-1.1.0.tgz
sed -i.bak s/broker.id=0/broker.id=$1/g kafka_2.12-1.1.0/config/server.properties
sed -i 's/log.dirs=\/tmp\/kafka-logs/logs.dir=..\/tmp\/kafka\/logs/g' kafka_2.12-1.1.0/config/server.properties
sed -i "s/zookeeper.connect=localhost:2181/zookeeper.connect=$2/g" kafka_2.12-1.1.0/config/server.properties