
rm -rf bin/

mkdir bin

javac src/K2D.java src/K2DAvg.java src/KAvg.java src/KCluster.java src/KData.java src/KDNA.java src/KDNAAvg.java src/KMeansMaster.java src/Util.java -d ./bin/

cp src/Main.java bin/Main.java

cd bin

mpijavac Main.java

rm bin/Main.java
