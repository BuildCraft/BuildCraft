cd ..

cmd /C recompile.bat

mkdir src\minecraft\net\minecraft\src\buildcraft\core\gui
mkdir src\minecraft\net\minecraft\src\buildcraft\factory\gui
mkdir src\minecraft\net\minecraft\src\buildcraft\transport\gui

copy src\minecraft\net\minecraft\src\buildcraft\core\gui\* bin\minecraft\net\minecraft\src\buildcraft\core\gui
copy src\minecraft\net\minecraft\src\buildcraft\core\gui\* bin\minecraft\net\minecraft\src\buildcraft\core\gui
copy src\minecraft\net\minecraft\src\buildcraft\core\gui\* bin\minecraft\net\minecraft\src\buildcraft\core\gui

rmdir /S /Q bin\minecraft\net\minecraft\src\buildcraft\core\gui\.svn
rmdir /S /Q bin\minecraft\net\minecraft\src\buildcraft\factory\gui\.svn
rmdir /S /Q bin\minecraft\net\minecraft\src\buildcraft\transport\gui\.svn

xcopy /Y /E buildcraft\buildcraft_client\bin\* bin\minecraft

cmd /C startclient.bat
