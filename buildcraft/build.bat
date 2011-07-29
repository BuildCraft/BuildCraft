set BCDIR=%CD%

rmdir /S /Q ..\src
mkdir ..\src
xcopy  /Y /E ..\src_work\* ..\src\

xcopy /Y /E buildcraft_client\src\net\* ..\src\minecraft\net
xcopy /Y /E buildcraft_server\src\net\* ..\src\minecraft_server\net

xcopy /Y /E common\net\* ..\src\minecraft\net
xcopy /Y /E common\net\* ..\src\minecraft_server\net

xcopy /Y /E ..\forge\forge_client\src\net\* ..\src\minecraft\net

xcopy /Y /E ..\forge\forge_common\net\* ..\src\minecraft\net
xcopy /Y /E ..\forge\forge_common\net\* ..\src\minecraft_server\net

cd ..
cmd /C recompile.bat
cd %BCDIR%

mkdir ..\bin\minecraft\net\minecraft\src\buildcraft\core\gui
mkdir ..\bin\minecraft\net\minecraft\src\buildcraft\factory\gui
mkdir ..\bin\minecraft\net\minecraft\src\buildcraft\transport\gui
mkdir ..\bin\minecraft\net\minecraft\src\buildcraft\builders\gui
mkdir ..\bin\minecraft\net\minecraft\src\buildcraft\energy\gui

xcopy /Y /E common\net\minecraft\src\buildcraft\core\gui\*.png ..\bin\minecraft\net\minecraft\src\buildcraft\core\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\factory\gui\*.png ..\bin\minecraft\net\minecraft\src\buildcraft\factory\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\transport\gui\*.png ..\bin\minecraft\net\minecraft\src\buildcraft\transport\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\builders\gui\*.png ..\bin\minecraft\net\minecraft\src\buildcraft\builders\gui
xcopy /Y /E common\net\minecraft\src\buildcraft\energy\gui\*.png ..\bin\minecraft\net\minecraft\src\buildcraft\energy\gui
