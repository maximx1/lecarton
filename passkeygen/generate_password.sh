#!/bin/bash

if [ "$1" = "build" ]
then
	javac -cp ".:./jbcrypt.jar" GenPasskey.java
elif [ "$1" = "run" ]
then
	java -cp ".:./jbcrypt.jar" GenPasskey $2
else
	echo "Usage: ./generate_password.sh build && ./generate_password.sh run <new_password>"
fi
