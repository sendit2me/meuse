#!/bin/bash

script_name=$0
script_full_path=$(dirname "$0")
cd $script_full_path

cd ..
docker build -t mirror_meuse .
cd mirror

sed -e "s|CURRENTUSERID|$(id -u)|" -e "s|CURRENTUSERGID|$(id -u)|" ../Dockerfile > ../Dockerfile
sed -e "s|CURRENTUSERID|$(id -u)|" -e "s|CURRENTUSERGID|$(id -u)|" ./docker-compose-base.yaml > ./docker-compose.yaml

if [[ ! -d ./registry ]]; then
    git clone git@github.com:sendit2me/crates.io-index.git registry
else
    git pull
fi
if [[ ! -d ./crates ]]; then
    mkdir -p ./crates
fi

docker-compose -f docker-compose.yaml up -d
