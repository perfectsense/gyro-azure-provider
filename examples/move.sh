#!/bin/bash

set -Eeuo pipefail

cd test
d=$(ls -d */)
cd ../

for i in $d; do
  echo "working with: " $i
  echo "n" | gyro up "test/"$i"create.gyro" || true

  if [[ -e ".gyro/state/test/"$i"create.gyro" ]]; then

    echo "state file exists: .gyro/state/test/"$i
    echo "running update"

    mv ".gyro/state/test/"$i"create.gyro" ".gyro/state/test/"$i"update.gyro"
    echo "n" | gyro up "test/"$i/"update.gyro" || true

    mv ".gyro/state/test/"$i"update.gyro" ".gyro/state/test/"$i"delete.gyro"
    echo "n" | gyro up "test/"$i"delete.gyro" || true
  fi
done
