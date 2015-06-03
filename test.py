import xml.etree.ElementTree as et
import re
import os
import sys
import shutil

forgeRoot = sys.argv [1]

root = et.parse (forgeRoot + ".classpath")
classpath = forgeRoot + "bin"

for e in root.findall (".//classpathentry"):
   path = e.attrib ["path"]
   classpath = classpath + ";" + path

testRoot = "testsuite"
testDir = testRoot + "/" + sys.argv [2]

os.chdir (testDir)

shutil.rmtree ("world", True)
shutil.copytree ("../base/world", "world")
shutil.copy ("../base/server.properties", "server.properties")

command = "java -Xincgc -Xmx1024M -Xms1024M -classpath \"" + classpath + "\" net.minecraftforge.fml.relauncher.ServerLaunchWrapper --nogui --world world --test test.seq --quit"
print command
os.system (command)
