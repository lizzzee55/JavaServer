#!/bin/bash
javac -Xlint:unchecked -cp ./lib/*:. ru/HttpServer.java
echo "--minifed js files--"
java -jar lib/yuicompressor-2.4.7pre.jar template/insertAds.js -o template/insertAds_compile.js
echo "run server"
err=1
until [ $err == 0 ];
do
    java -cp ./lib/*:  ru/HttpServer
    echo "error - restart server";
    err=$?
done
