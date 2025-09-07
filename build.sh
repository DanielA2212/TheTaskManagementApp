#!/bin/bash

# Build script for Task Management Application

echo "Building Task Management Application..."

# Create directories
mkdir -p build/classes
mkdir -p build/jar

# Compile Java sources
echo "Compiling Java sources..."
javac -cp "lib/*" -d build/classes src/main/java/com/taskmanager/**/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Create JAR file
    echo "Creating JAR file..."
    cd build/classes
    jar cfe ../jar/task-management-app.jar com.taskmanager.view.TaskManagementGUI com/taskmanager/**/*.class
    cd ../..
    
    # Copy H2 JAR to build directory
    cp lib/h2.jar build/jar/
    
    echo "Build completed successfully!"
    echo "Executable JAR: build/jar/task-management-app.jar"
    echo "To run: java -cp 'build/jar/*' com.taskmanager.view.TaskManagementGUI"
else
    echo "Compilation failed!"
    exit 1
fi
