#!/bin/bash -e
mcp=mcp*.zip
cp $mcp $(basename $mcp .zip)-forged.zip
mkdir -p build/tmp/forge
cd build/tmp/forge
unzip -o ../../../forge*.zip forge/fml/conf/\*
cd forge/fml
zip -r ../../../../../mcp*-forged.zip conf

