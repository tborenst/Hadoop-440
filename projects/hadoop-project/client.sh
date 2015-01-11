#!/bin/bash

clear

echo "Starting MapReduce client machine - connecting to localhost:15437 ..."
cd src
java -cp .:../libs/jackson-all-1.9.11.jar system.Main -c localhost:15437 ../WorkDir
cd ..
