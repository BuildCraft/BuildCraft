cd ..

rmdir /S /Q src
mkdir src
xcopy  /Y /E src_work\* src\

xcopy /Y /E buildcraft\buildcraft_client\src\net\* src\minecraft\net
xcopy /Y /E buildcraft\buildcraft_server\src\net\* src\minecraft_server\net

xcopy /Y /E buildcraft\common\net\* src\minecraft\net
xcopy /Y /E buildcraft\common\net\* src\minecraft_server\net

cmd /C recompile.bat

mkdir bin\minecraft\net\minecraft\src\buildcraft\core\gui
mkdir bin\minecraft\net\minecraft\src\buildcraft\factory\gui
mkdir bin\minecraft\net\minecraft\src\buildcraft\transport\gui

xcopy /Y /E buildcraft\common\net\minecraft\src\buildcraft\core\gui\*.png bin\minecraft\net\minecraft\src\buildcraft\core\gui
xcopy /Y /E buildcraft\common\net\minecraft\src\buildcraft\factory\gui\*.png bin\minecraft\net\minecraft\src\buildcraft\factory\gui
xcopy /Y /E buildcraft\common\net\minecraft\src\buildcraft\transport\gui\*.png bin\minecraft\net\minecraft\src\buildcraft\transport\gui

cmd /C startclient.bat
