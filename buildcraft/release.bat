cmd /C build.bat

cd ..

cmd /C reobfuscate.bat

set PATH=E:\cygwin\bin;%PATH%

cd buildcraft

sh package.sh 1.6.6.4

pause
