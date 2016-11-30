#!/usr/bin/env bash

lein dist
cp -r ./resources/* ./docs
cp -r ./target/dist/resources/* ./docs
echo "Ready"
