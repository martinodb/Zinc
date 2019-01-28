#!/bin/bash

# https://gist.github.com/octasimo/66f3cc230725d1cf1421

#Whenever you clone a repo, you do not clone all of its branches by default.
#If you wish to do so, use the following script:

for branch in `git branch -a | grep remotes | grep -v HEAD | grep -v master `; do
   git branch --track ${branch#remotes/origin/} $branch
done
