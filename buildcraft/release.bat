set BCDIR=%CD%

cmd /C build.bat

cd ..
cmd /C reobfuscate.bat
cd %BCDIR%

set PATH=E:\cygwin\bin;%PATH%

sh package.sh 2.0.1

pause
