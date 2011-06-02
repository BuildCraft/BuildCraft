rm minecraft.patch
touch minecraft.patch

diff -u ../src_base ../src -r | tr -d '\r' \
      >> minecraft.patch
