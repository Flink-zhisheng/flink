#!/bin/bash
git checkout release-1.9
git pull
git checkout release-1.9-zhisheng
git merge release-1.9
git push
