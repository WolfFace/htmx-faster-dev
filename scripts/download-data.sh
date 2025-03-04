#!/bin/bash

URL="https://github.com/ethanniser/NextFaster/raw/refs/heads/main/data/data.zip"  # Replace with your actual URL
TEMP_FILE="temp_download.zip"
TARGET_DIR="./init-scripts"

# Create ext directory if it doesn't exist
mkdir -p "$TARGET_DIR"

# Download the file
echo "Downloading from $URL..."
if command -v curl &> /dev/null; then
    curl -L -o "$TEMP_FILE" "$URL"
elif command -v wget &> /dev/null; then
    wget -O "$TEMP_FILE" "$URL"
else
    echo "Error: Neither curl nor wget is installed"
    exit 1
fi

# Check if download was successful
if [ $? -ne 0 ]; then
    echo "Error: Download failed"
    rm -f "$TEMP_FILE"
    exit 1
fi

# Extract the zip file
echo "Extracting to $TARGET_DIR..."
if ! unzip -o "$TEMP_FILE" -d "$TARGET_DIR"; then
    echo "Error: Extraction failed"
    rm -f "$TEMP_FILE"
    exit 1
fi

# Clean up
rm -f "$TEMP_FILE"
echo "Download and extraction completed successfully"
