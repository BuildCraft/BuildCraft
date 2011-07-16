cmd /C build.bat

cd ..

cmd /C reobfuscate.bat

set PATH=E:\cygwin\bin;%PATH%

cd buildcraft

sh package.sh 1.7.3.2

pause
