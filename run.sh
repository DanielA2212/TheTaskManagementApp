#!/bin/bash

# Run script for Task Management Application

echo "Starting Task Management Application..."

# Check if JAR exists
if [ ! -f "build/jar/task-management-app.jar" ]; then
    echo "JAR file not found. Building first..."
    ./build.sh
fi

# Run the application
java -cp 'build/jar/*' com.taskmanager.view.TaskManagementGUI
