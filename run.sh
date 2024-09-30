#!/bin/bash

# Step 1: Clean the project
./gradlew clean

# Step 2: Build the fat jar
./gradlew fatJar

# Step 3: Init props
BOT_TOKEN=your_actual_bot_token
BOT_USERNAME=your_actual_bot_tag
DB_URL=your_actual_supabase_url
DB_KEY=your_actual_supabase_api_key

# Step 4: Run the jar
java -DBOT_TOKEN=$BOT_TOKEN -DBOT_USERNAME=$BOT_USERNAME -DDB_URL=$DB_URL -DDB_KEY=$DB_KEY -jar ./build/libs/lolers_bot-1.0-fat.jar