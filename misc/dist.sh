# This script requires a copy of pngout and kzip.
# THIS SCRIPT IS HIGHLY TEMPORARY - SHOULD BE REPLACED WITH A GRADLE VERSION

#!/bin/sh
rm -rf dist
mkdir -p dist/tmp
mkdir -p dist/misc
mkdir -p dist/modules
cd dist
cp ../build/libs/buildcraft-$1* .
cd tmp
unzip ../../build/libs/buildcraft-$1.jar
rm ../buildcraft-$1.jar

for i in `find -name *.png`; do ../../tools/pngout "$i"; done
../../tools/kzip -r -y ../buildcraft-$1.jar *

../../tools/kzip -r -y ../modules/buildcraft-$1-core.jar assets/buildcraft assets/buildcraftcore buildcraft/BuildCraftCore* buildcraft/core \
	buildcraft/BuildCraftMod* buildcraft/api \
	cofh LICENSE* changelog mcmod.info versions.txt
../../tools/kzip -r -y ../modules/buildcraft-$1-builders.jar assets/buildcraftbuilders buildcraft/BuildCraftBuilders* buildcraft/builders LICENSE
../../tools/kzip -r -y ../modules/buildcraft-$1-energy.jar assets/buildcraftenergy buildcraft/BuildCraftEnergy* buildcraft/energy LICENSE
../../tools/kzip -r -y ../modules/buildcraft-$1-factory.jar assets/buildcraftfactory buildcraft/BuildCraftFactory* buildcraft/factory LICENSE
../../tools/kzip -r -y ../modules/buildcraft-$1-robotics.jar assets/buildcraftrobotics buildcraft/BuildCraftRobotics* buildcraft/robotics LICENSE
../../tools/kzip -r -y ../modules/buildcraft-$1-silicon.jar assets/buildcraftsilicon buildcraft/BuildCraftSilicon* buildcraft/silicon LICENSE
../../tools/kzip -r -y ../modules/buildcraft-$1-transport.jar assets/buildcrafttransport buildcraft/BuildCraftTransport* buildcraft/transport LICENSE

cd ..
rm -rf tmp
cd ..
