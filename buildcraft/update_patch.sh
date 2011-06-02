rm minecraft.patch
touch minecraft.patch

diff -u ../src_base ../src -r --strip-trailing-cr | tr -d '\r' \
      >> minecraft.patch
