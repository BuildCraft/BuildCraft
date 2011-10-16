set BCDIR=%CD%

cmd /C build.bat

cd ..
cmd /C reobfuscate.bat
cd %BCDIR%

set PATH=C:\cygwin\bin;%PATH%

sh package.sh 2.2.3-beta

pause
