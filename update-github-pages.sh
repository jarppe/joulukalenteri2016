#!/usr/bin/env bash

lein dist
rm -fr ./docs/*
cp -r ./resources/* ./docs
cp -r ./target/dist/resources/* ./docs
echo "Ready"
