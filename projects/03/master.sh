#!/bin/bash

clear

echo "Starting MapReduce master server: slave port 15237, client port 15437, WorkDir: ./WorkDir ..."
cd src
java -cp .:../libs/jackson-all-1.9.11.jar system.Main -m 15237 15437 ../WorkDir
cd ..

clear
