
rm -rf bin/

mkdir bin

mpijavac src/Main.java src/tests/K2D.java src/tests/K2DAvg.java src/tests/KDNA.java src/tests/KDNAAvg.java src/parallel/*.java src/util/*.java -d ./bin/

