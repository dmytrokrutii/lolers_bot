#!/bin/bash

# Step 1: Clean the project
./gradlew clean

# Step 2: Build the fat jar
./gradlew fatJar

# Step 3: Run the jar with specified system properties
java -Dtoken={TOKEN} -Dusername={TAG} -jar ./build/libs/lolers_bot-1.0-fat.jar