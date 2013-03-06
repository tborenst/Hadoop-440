#!/bin/bash

clear


cd src
echo "Example 1 - basic usage..."
java -cp . examples.Example1
echo "Example 2 - pass by reference and bind..."
java -cp . examples.Example2
echo "Example 3 - error passing..."
java -cp . examples.Example3

cd ..

