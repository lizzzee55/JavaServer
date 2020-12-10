#!/bin/bash

echo "--minifed js files--"
java -jar server/lib/yuicompressor-2.4.7pre.jar server/template/insertAds.js -o server/template/insertAds_compile.js
echo "run server"
err=1
until [ $err == 0 ];
do
    java -cp server/lib/*:server/server.jar ru.HttpServer
    echo "error - restart server";
    err=$?
done
