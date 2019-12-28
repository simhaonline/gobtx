#!/bin/bash
base_dir=$(dirname $(pwd))
buildDir=$(pwd)
echo $buildDir

bte="build_btx_backend"
BRANCH="master"

while [ $# -gt 0 ]; do
  COMMAND=$1
  case $COMMAND in
  -compress)
    COMPRESS="TURE"
    shift 1
    ;;
  -build)
    BUILD_NAME=$2
    shift 2
    ;;
  -branch)
    BRANCH=$2
    shift 2
    ;;
  *)
    break
    ;;
  esac
done

echo "use branch => $BRANCH"
git checkout $BRANCH
git pull

ISFULL=""
if [ -z "$BUILD_NAME" ]; then
  ${buildDir}/gradlew build -x test
  modules=("xchange" "ws_server")
  if [ -d "$base_dir/$bte" ]; then
    rm -rf "$base_dir/$bte"
  fi
  mkdir -p $base_dir/$bte

  cp -r bin "$base_dir/$bte"
  chmod -R 755 "$base_dir/$bte/"
  ISFULL="TRUE"
else
  ARR=$(echo $BUILD_NAME | tr "," "\n")
  modules=$ARR
  for x in $ARR; do
    echo "gradle $x:build -x test"
    ${buildDir}/gradlew $x:build -x test
  done
  mkdir -p $base_dir/$bte
  rm -rf "$base_dir/$bte/bin"
  cp -r bin "$base_dir/$bte"
  chmod -R 755 "$base_dir/$bte/"
fi

function pkgModule() {
  mn=$1
  if [ -d "$base_dir/$bte/$mn" ]; then
    rm -rf "$base_dir/$bte/$mn"
  fi
  mkdir -p $base_dir/$bte/$mn
  cd $mn
  if [ -d "$(pwd)/build" ]; then
    echo "copy $(pwd)/build/resources to $base_dir/$bte/$mn"
    cp -r build "$base_dir/$bte/$mn"
    rm -rf "$base_dir/$bte/$mn/build/classes"
    rm -rf "$base_dir/$bte/$mn/build/tmp"
    rm -rf "$base_dir/$bte/$mn/build/libs"
    rm -rf "$base_dir/$bte/$mn/build/generated"
    mkdir -p $base_dir/$bte/$mn/build/libs
    echo "copy $(pwd)/build/libs/*.jar to $base_dir/$bte/$mn"
    cp build/libs/*.jar $base_dir/$bte/$mn/build/libs
  fi
  echo "$mn is done"
  cd ../
  if [ -n "$COMPRESS" ]; then
    cd $base_dir/$bte/
    echo "$(pwd)"
    echo "tar -czvf ${mn}.tar.gz ${mn}"
    tar -czvf ${mn}.tar.gz ${mn}
    cd $buildDir
  fi
}

for project in ${modules[*]}; do
  pkgModule $project
done

function pkgFrontEnd() {
  mn='xchange_frontend'
  if [ -d "$base_dir/$bte/$mn" ]; then
    rm -rf "$base_dir/$bte/$mn"
  fi
  echo "mkdir -p $base_dir/$bte/$mn"
  mkdir -p $base_dir/$bte/$mn
  cd $mn
  rm -rf ./build

  echo "build xchange_frontend >>>>>>>>>>>>>>"

  npm i
  grunt build

  echo "build xchange handbook>>>>>>>>>>>>>>>>"
  cd ../handbook
  #npm i gitbook-cli
  gitbook install
  gitbook build

  rm ./_book/.gitignore

  cd ../$mn

  if [ -d "$(pwd)/build" ]; then
    echo "copy $(pwd)/build to $base_dir/$bte/$mn"
    cp -r build "$base_dir/$bte/$mn"

    echo "cp -r ../handbook/_book build   #this is to keep the copy"

    cp -r ../handbook/_book build
    mv build/_book build/book

    echo "cp -r ../handbook/_book $base_dir/$bte/$mn/build #this is to make the build modules cached"

    cp -r ../handbook/_book "$base_dir/$bte/$mn/build"
    mv "$base_dir/$bte/$mn/build/_book" "$base_dir/$bte/$mn/build/book"
  fi
  echo "$mn is done"
  cd ../

  #cd $base_dir/$bte/
  #echo "$(pwd)"
  #echo "tar -czvf ${mn}.tar.gz ${mn}"
  #tar -czvf ${mn}.tar.gz ${mn}
  #cd $buildDir
}

pkgFrontEnd

echo 'done'
