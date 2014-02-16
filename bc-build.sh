VERSION=5.0.0
MC_VERSION=1.7.2
FORGE_VERSION=10.12.0.1024
BUILD_NUMBER=58

build_dir=`pwd`/build-$VERSION
forge_archive=forge-$MC_VERSION-$FORGE_VERSION-src.zip

rm -rf $build_dir
mkdir $build_dir
mkdir $build_dir/forge

cd $build_dir/forge

wget http://files.minecraftforge.net/maven/net/minecraftforge/forge/$MC_VERSION-$FORGE_VERSION/$forge_archive

unzip $forge_archive

./gradlew setupDecompWorkspace

rm -rf src/*
mkdir src/main
cp -r ../../common src/main/java
cp -r ../../buildcraft_resources src/main/resources

(
cd src/main
for j in `find .` ; do 
   if [ ! -d $j ]; then
      case $j in
         *Version.java)
            sed "s/@VERSION@/$VERSION/g" $j > $j.tmp && mv $j.tmp $j
            sed "s/@MC_VERSION@/$MC_VERSION/g" $j > $j.tmp && mv $j.tmp $j
            sed "s/@BUILD_NUMBER@/$BUILD_NUMBER/g" $j > $j.tmp && mv $j.tmp $j
	    ;;
	 *.info)
            sed "s/@VERSION@/$VERSION/g" $j > $j.tmp && mv $j.tmp $j
            sed "s/@MC_VERSION@/$MC_VERSION/g" $j > $j.tmp && mv $j.tmp $j
            sed "s/@BUILD_NUMBER@/$BUILD_NUMBER/g" $j > $j.tmp && mv $j.tmp $j
	 ;;
      esac
   fi
done
)

./gradlew reobf

(
cd build/libs
mv *.jar $build_dir/buildcraft-$MC_VERSION-$VERSION.jar
)
