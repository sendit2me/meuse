#!/bin/bash

#
docker build -t nexus_nexus .

sed -e "s|CURRENTUSERID|$(id -u)|" -e "s|CURRENTUSERGID|$(id -u)|" ./DockerfileBase > ./Dockerfile
sed -e "s|CURRENTUSERID|$(id -u)|" -e "s|CURRENTUSERGID|$(id -u)|" ./docker-compose-base.yaml > ./docker-compose.yaml

if [[ ! -d ./nexus-data ]]; then
    mkdir ./nexus-data
fi

if [[ ! -d ./registry ]]; then
    git clone git@github.com:sendit2me/crates.io-index.git registry
else
    git pull
fi
if [[ ! -d ./crates ]]; then
    mkdir -p ./crates
fi

docker-compose -f docker-compose.yaml up -d
