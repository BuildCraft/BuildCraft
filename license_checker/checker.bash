#!/bin/bash
# License checker program
# Add users to "agreed.txt" if someone has agreed to the relicense
# Add users to "unused_code.txt" if someone hasn't agreed to the relicese (yet) but their code isn't used anymore
# Relicensable files will be output to "out/safe/[path]"
# Files that need people to sign will be in "out/need/[path]"

# NOTE THAT THE OUT DIR IS REMOVED EVERYTIME!

## CONSTANTS

agreeInput="license_checker/agreed.txt"
unusedInput="license_checker/unused_code.txt"

outDir="license_checker/out"
agreedFile="license_checker/out/merged_agreed.txt"
tempOutDir="license_checker/out/work"
goodOutDir="license_checker/out/safe"
badOutDir="license_checker/out/need"

## FUNCTIONS

dispProgress() {
    sp1="            "
    sp2=$sp1$sp1$sp1
    sp3=$sp2$sp2$sp2
    printf "> $1$sp3\r"
}

testFile() {
    # arg 1 is the file

    # Command: "git log --follow --pretty=short  $1 | grep Author | sort | uniq > "$tempOutDir/$2.all"
    # Command breakdown:
    # "git log --follow --pretty=short  $1" // get a full log
    # sort                                  // So that the output looks much better. Also uniq doesn't work without this
    # uniq                                  // Remove all but 1 of the duplicates
    local fld=$(dirname $1)
    local fle=$(basename $1)
    dispProgress "$1"
    ( cd $fld && eval "git log --follow --pretty=format:\"%an <%ae>\" -- $fle | sort | uniq" ) > "$tempOutDir/$1.all"
    eval "grep -vf $agreedFile $tempOutDir/$1.all" > "$tempOutDir/$1.req"
    if [ -s "$tempOutDir/$1.req" ]; then
        cat "$tempOutDir/$1.req" > "$badOutDir/$1.req"
    else
        cat "$tempOutDir/$1.all" > "$goodOutDir/$1.all"
    fi
    return
}

testFolder() {
    # arg 1 is the file
    for file in $tempOutDir/$1/*
    do
        if [ -d $file ]; then
            eval "cat $file/_.all" >> "$tempOutDir/$1/_tmp.all"
        elif [ $file != "$tempOutDir/$1/_tmp.all" ]; then
            eval "cat $file" >> "$tempOutDir/$1/_tmp.all"
        fi
    done
    eval "cat $tempOutDir/$1/_tmp.all | sort | uniq" > "$tempOutDir/$1/_.all"
    eval "grep -vf $agreedFile $tempOutDir/$1/_.all" > "$tempOutDir/$1/_.req"
    if [ -s "$tempOutDir/$1/_.req" ]; then
        cat "$tempOutDir/$1/_.req" > "$badOutDir/$1/_.req"
    else
        cat "$tempOutDir/$1/_.all" > "$goodOutDir/$1/_.all"
    fi
    return
}

cleanFile() {
    # arg 1 is the file
    if [ "$(ls -A $1)" ]; then
        return
    else
        rm $1 -r
    fi
    return
}

scanFiles() {
    # arg 1 is the file
    for file in $1/*
    do
        local f1=$file
        if [ -d $file ]; then
            mkdir "$tempOutDir/$file"
            mkdir "$goodOutDir/$file"
            mkdir "$badOutDir/$file"
            scanFiles $f1
            testFolder $f1
            cleanFile $goodOutDir/$f1
            cleanFile $badOutDir/$f1
        else
            testFile "$f1"
        fi
    done
    return
}

scanFolder() {
    local f1=$1
    mkdir "$tempOutDir/$1"
    mkdir "$goodOutDir/$1"
    mkdir "$badOutDir/$1"
    scanFiles $f1
    testFolder $f1
}

## MAIN

# Because all of our paths start with ".." we need to remove that.
# A hacky way is to prepend all paths with "temp/" so that the ".." cancels out.

cd ".."

rm $outDir -r
mkdir $outDir
echo "THIS FOLDER IS MACHINE GENERATED" > "$outDir/DO_NOT_EDIT"
echo " " >> "$outDir/DO_NOT_EDIT"
echo "Just the \"out\" directory and sub directorues" >> "$outDir/DO_NOT_EDIT"
echo "Every time \"checker.bash\" is run this folder is cleaned and regenerated" >> "$outDir/DO_NOT_EDIT"
mkdir $tempOutDir
mkdir $goodOutDir
mkdir $badOutDir

cat $agreeInput >> $agreedFile
cat $unusedInput >> $agreedFile

scanFolder "common"
scanFolder "common_old_license"
scanFolder "src_old_license"
scanFolder "BuildCraft-Localization"
scanFolder "buildcraft_resources"
testFolder "."

eval "git log --pretty=format:\"%an <%ae>\" -- $fle | sort | uniq" > "license_checker/out/all"
eval "grep -vf $agreedFile license_checker/out/all" > "license_checker/out/req"

echo "done                                                                              "
