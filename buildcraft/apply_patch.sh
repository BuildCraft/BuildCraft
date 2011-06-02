if [ ! -f ../src_base ] then
   cp -r ../src ../src_base
   patch -p0 apply_patch.sh
else
   echo "patch already applied - remove src and src_base and decompile again"
fi
