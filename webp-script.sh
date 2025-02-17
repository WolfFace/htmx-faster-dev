#!/bin/sh

# Install required packages
apk add --no-cache imagemagick wget

# Ensure output directory exists
mkdir -p output

# Process each URL
while IFS= read -r url; do
    # Extract filename from URL
    filename=$(basename "$url")
    name=$(basename "$url" .png)

    # Check if both target files already exist
    if [ -f "output/81__$name.webp" ] && [ -f "output/256__$name.webp" ]; then
        echo "Skipping $filename - already processed"
        continue
    fi

    # Download image
    wget -q "$url" -O "temp_$filename"

    # Convert to WebP in two sizes
    convert "temp_$filename" -resize 81x -quality 80 "output/81__$name.webp"
    convert "temp_$filename" -resize 256x -quality 80 "output/256__$name.webp"

    # Clean up temporary file
    rm "temp_$filename"

    echo "Processed $filename"
done < urls.txt
