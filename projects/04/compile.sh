
rm -rf bin/

mkdir bin

mpijavac src/Main.java src/tests/K2D.java src/tests/K2DAvg.java src/util/KAvg.java src/util/KCluster.java src/util/KData.java src/tests/KDNA.java src/tests/KDNAAvg.java src/parallel/KMeansMaster.java src/serial/KMeans.java src/util/Util.java -d ./bin/

