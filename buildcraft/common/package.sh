cd ../reobf

cd minecraft

version=$1

rm *.zip

zip -r buildcraft-ssp-core-$version.zip mod_BuildCraftCore* BuildCraftBlockUtil.class buildcraft/core net/minecraft/src/buildcraft/core

zip -r buildcraft-ssp-factory-$version.zip mod_BuildCraftFactory.class buildcraft/factory net/minecraft/src/buildcraft/factory

zip -r buildcraft-ssp-transport-$version.zip mod_BuildCraftTransport.class buildcraft/transport net/minecraft/src/buildcraft/transport

zip -r buildcraft-ssp-devel-$version.zip mod_BuildCraftDevel.class buildcraft/devel


