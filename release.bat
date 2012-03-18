set BCDIR=%CD%

cmd /C build.bat

cd ..
cmd /C reobfuscate.bat
cd %BCDIR%

mkdir ..\reobf\minecraft\net\minecraft\src\buildcraft\core\gui
mkdir ..\reobf\minecraft\net\minecraft\src\buildcraft\factory\gui
mkdir ..\reobf\minecraft\net\minecraft\src\buildcraft\transport\gui
mkdir ..\reobf\minecraft\net\minecraft\src\buildcraft\builders\gui
mkdir ..\reobf\minecraft\net\minecraft\src\buildcraft\energy\gui

xcopy /Y /E common\net\minecraft\src\buildcraft\core\gui\*.png ..\reobf\minecraft\net\minecraft\src\buildcraft\core\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\factory\gui\*.png ..\reobf\minecraft\net\minecraft\src\buildcraft\factory\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\transport\gui\*.png ..\reobf\minecraft\net\minecraft\src\buildcraft\transport\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\builders\gui\*.png ..\reobf\minecraft\net\minecraft\src\buildcraft\builders\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\energy\gui\*.png ..\reobf\minecraft\net\minecraft\src\buildcraft\energy\gui


set PATH=E:\cygwin\bin;%PATH%

sh package.sh 3.1.3

pause
