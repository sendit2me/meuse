#!/bin/bash

if [[ ! -d ./registry ]]; then
    git clone git@github.com:sendit2me/crates.io-index.git registry
else
    git pull
fi
if [[ ! -d ./crates ]]; then
    mkdir -p ./crates
fi

docker-compose -f docker-compose.yaml up