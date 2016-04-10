#!/bin/sh
cd ..
# Location to font file. Different on different systems.
font_location="$1"
for i in 11 13 16 18 22
do
    grub-mkfont -r 0-383,1024-1119 -o buildcraft_resources/assets/buildcraftcore/guide/font/$i.pf2 -s i $font_location/DejaVuSans.ttf
done
