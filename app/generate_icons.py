#!/usr/bin/env python3
from PIL import Image
import os

# Icon sizes for different densities
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

# Load the source image
source_image = 'appicon.jpeg'
img = Image.open(source_image)

# Convert to RGBA if needed
if img.mode != 'RGBA':
    img = img.convert('RGBA')

# Generate icons for each density
for folder, size in sizes.items():
    output_dir = f'mobile/src/main/res/{folder}'
    os.makedirs(output_dir, exist_ok=True)
    
    # Resize image
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    
    # Save as PNG
    output_path = f'{output_dir}/ic_launcher.png'
    resized.save(output_path, 'PNG')
    print(f'Created {output_path}')

print('All icons generated successfully!')
