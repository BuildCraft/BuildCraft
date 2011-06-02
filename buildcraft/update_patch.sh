server_files="EntityTrackerEntry.java EntityTracker.java"

rm minecraft.patch
touch minecraft.patch

for f in $server_files ; do
   diff -u ../src_base/minecraft_server/net/minecraft/src/$f \
      ../src/minecraft_server/net/minecraft/src/$f -r \
      | tr -d '\r' \
      >> minecraft.patch
done
