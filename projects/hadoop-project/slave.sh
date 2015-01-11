#!/bin/bash

clear

echo "Starting MapReduce slave machine - connecting to localhost:15237 ..."
cd src
java -cp . system.Main -s localhost:15237 ../WorkDir
cd ..

clear
echo "Quit MapReduce program."


