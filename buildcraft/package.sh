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

zip -r $dir/buildcraft-core-$version.zip mod_BuildCraftCore* BuildCraftCore.class RenderPassiveItem.class BuildCraftBlockUtil.class buildcraft/core net/minecraft/src/buildcraft/core

zip -r $dir/buildcraft-transport-$version.zip mod_BuildCraftTransport.class BuildCraftTransport.class buildcraft/transport net/minecraft/src/buildcraft/transport

zip -r $dir/buildcraft-factory-$version.zip mod_BuildCraftFactory.class buildcraft/factory net/minecraft/src/buildcraft/factory

zip -r $dir/buildcraft-buiders-$version.zip mod_BuildCraftBuilders.class buildcraft/builders net/minecraft/src/buildcraft/builders

zip -r $dir/buildcraft-devel-$version.zip mod_BuildCraftDevel.class buildcraft/devel


