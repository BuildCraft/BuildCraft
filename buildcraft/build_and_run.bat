cd ..

cmd /C recompile.bat

xcopy /Y /E buildcraft\buildcraft_client\bin\* bin\minecraft
xcopy /Y /E buildcraft\buildcraft_server\bin\* bin\minecraft_server


rmdir /S /Q bin\minecraft\net\minecraft\src\buildcraft\core\gui\.svn
rmdir /S /Q bin\minecraft\net\minecraft\src\buildcraft\factory\gui\.svn
rmdir /S /Q bin\minecraft\net\minecraft\src\buildcraft\transport\gui\.svn

xcopy /Y /E buildcraft\buildcraft_client\bin\* bin\minecraft

cmd /C startclient.bat
