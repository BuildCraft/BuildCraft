#!/bin/bash
# License checker program
# Will attempt to find all commit id given a line from "*.req"
# OR will attempt to find a commit given an email or something

eval "git log --pretty=format:\"%H %an <%ae>\" | grep \"$1\""
