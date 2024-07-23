#!/bin/bash

if [[ -d "test" ]]; then
  echo "Test Directory exists"
else
  mkdir test
fi

for i in $(ls -d */); do
  echo "working with: " $i
  if [[ "$i" != "test/" ]]; then
    for j in $(ls $i); do
      echo $i$j
      newdir=${j%".gyro"}
      echo $newdir

      if [[ -d "test/"$newdir ]]; then
        echo "Directory $newdir exists"
      else
        mkdir "test/"$newdir
      fi

      cp $i$j "test/"$newdir
      mv "test/"$newdir/$j "test/"$newdir/create.gyro
      cp $i$j "test/"$newdir
      mv "test/"$newdir/$j "test/"$newdir/update.gyro
      touch "test/"$newdir/delete.gyro
    done
  fi
  echo
done
