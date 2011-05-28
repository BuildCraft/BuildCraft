cd ../reobf

cd minecraft

version=$1

function removesvn () {
  (
  cd $1

  if [ -d .svn ]; then
     rm -rf .svn
  fi

  for j in `ls`
  do
     if [ -d $j ]; then
        removesvn $j
     fi
  done
  )
}

removesvn .

dir=../../release-$version

rm -rf $dir
mkdir $dir

zip -r $dir/buildcraft-A-core-$version.zip mod_BuildCraftCore* BuildCraftCore.class RenderPassiveItem.class BuildCraftBlockUtil.class buildcraft/core net/minecraft/src/buildcraft/core buildcraft/api

zip -r $dir/buildcraft-B-transport-$version.zip mod_BuildCraftTransport.class BuildCraftTransport.class buildcraft/transport net/minecraft/src/buildcraft/transport buildcraft/api

zip -r $dir/buildcraft-B-factory-$version.zip mod_BuildCraftFactory.class buildcraft/factory net/minecraft/src/buildcraft/factory buildcraft/api

zip -r $dir/buildcraft-B-builders-$version.zip mod_BuildCraftBuilders.class buildcraft/builders net/minecraft/src/buildcraft/builders buildcraft/api

zip -r $dir/buildcraft-C-devel-$version.zip mod_BuildCraftDevel.class buildcraft/devel buildcraft/api

zip -r $dir/buildcraft-api-$version.zip buildcraft/api


