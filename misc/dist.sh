# This script requires a copy of pngout, kzip and jq.
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


# Function to extract modules mcmod.info data
modinfo(){
	modinfo=$(../../tools/jq ".[] | select( .modid == \"$1\" )" mcmod.info.json)
	echo "[$modinfo]" > mcmod.info
	clear modinfo
}

# Move mcmod.info for safe keeping
mv mcmod.info mcmod.info.json

modinfo "BuildCraft|Core"
../../tools/kzip -r -y ../modules/buildcraft-$1-core.jar assets/buildcraft assets/buildcraftcore buildcraft/BuildCraftCore* buildcraft/core \
	buildcraft/BuildCraftMod* buildcraft/api \
	cofh LICENSE* changelog mcmod.info versions.txt

modinfo "BuildCraft|Builders"
../../tools/kzip -r -y ../modules/buildcraft-$1-builders.jar assets/buildcraftbuilders buildcraft/BuildCraftBuilders* buildcraft/builders LICENSE mcmod.info

modinfo "BuildCraft|Energy"
../../tools/kzip -r -y ../modules/buildcraft-$1-energy.jar assets/buildcraftenergy buildcraft/BuildCraftEnergy* buildcraft/energy LICENSE mcmod.info

modinfo "BuildCraft|Factory"
../../tools/kzip -r -y ../modules/buildcraft-$1-factory.jar assets/buildcraftfactory buildcraft/BuildCraftFactory* buildcraft/factory LICENSE mcmod.info

modinfo "BuildCraft|Robotics"
../../tools/kzip -r -y ../modules/buildcraft-$1-robotics.jar assets/buildcraftrobotics buildcraft/BuildCraftRobotics* buildcraft/robotics LICENSE mcmod.info

modinfo "BuildCraft|Silicon"
../../tools/kzip -r -y ../modules/buildcraft-$1-silicon.jar assets/buildcraftsilicon buildcraft/BuildCraftSilicon* buildcraft/silicon LICENSE mcmod.info

modinfo "BuildCraft|Transport"
../../tools/kzip -r -y ../modules/buildcraft-$1-transport.jar assets/buildcrafttransport buildcraft/BuildCraftTransport* buildcraft/transport LICENSE mcmod.info

# Move back mcmod.info
mv mcmod.info.json mcmod.info

cd ..
rm -rf tmp
cd ..
