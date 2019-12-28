#!/bin/bash
base_dir=$(dirname $(pwd))
runDir=$(pwd)

BRANCH="master"
MODULE_FILE_NAME="module"

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
  -use_gradle_wrapper)
    USD_GRADLE_WRAPPER="TRUE"
    shift 1
    ;;
  -project_path)
    PROJECT_PATH=$2
    shift 2
    ;;
  -builded_path)
    BUILDED_PATH=$2
    shift 2
    ;;
  -module_file_name)
    MODULE_FILE_NAME=$2
    shift 2
    ;;
  -module_file_path)
    MODULE_FILE_PATH=$2
    shift 2
    ;;
  *)
    break
    ;;
  esac
done

if [ -z "$MODULE_FILE_PATH" ]; then
  MODULE_FILE_PATH=$PROJECT_PATH
fi

#MODULE_FILE="${MODULE_FILE_PATH}/${MODULE_FILE_NAME}"
MODULE_FILE="$MODULE_FILE_PATH${MODULE_FILE_NAME}"

declare -A map

echo "Try pop from module properties ${MODULE_FILE}"

INDEX=0
modules=()
for kv in $(grep -P -o "[^=\s]+\s*=\s*[^=\s]+" ${MODULE_FILE} | sed s/[[:space:]]//g); do
  OLD_IFS="$IFS"
  IFS="="
  TARR=($kv)
  IFS="$OLD_IFS"
  echo "${TARR[0]} => ${TARR[1]}"
  map[${TARR[0]}]=${TARR[1]}
  modules[$INDEX]=${TARR[0]}
  ((INDEX = INDEX + 1))

done

echo "The target project path $PROJECT_PATH"
cd $PROJECT_PATH
echo "use branch => $BRANCH"
git checkout $BRANCH
git pull
#source  ~/.profile
#export gradle='/opt/gradle/gradle-5.4.1/bin/gradle'

ISFULL=""
if [ -z "$BUILD_NAME" ]; then
  echo "FULL_BUILD   ${USD_GRADLE_WRAPPER}"
  sh ${PROJECT_PATH}/.build.sh
  if [ "$USD_GRADLE_WRAPPER" == "TRUE" ]; then
    ${PROJECT_PATH}/gradlew clean build -x test
  else
    gradle build -x test
  fi
  if [ -d "$BUILDED_PATH" ]; then
    rm -rf "$BUILDED_PATH"
  fi
  mkdir -p $BUILDED_PATH

  cp -r bin "$BUILDED_PATH"
  chmod -R 755 "$BUILDED_PATH/"
  ISFULL="TRUE"
else
  OLD_IFS="$IFS"
  IFS=","
  modules=($BUILD_NAME)
  IFS="$OLD_IFS"
  for x in ${modules[*]}; do
    echo "gradle $x:build -x test"
    if [ "$USD_GRADLE_WRAPPER" == "TRUE" ]; then
      ${PROJECT_PATH}/gradlew $x:build -x test
    else
      gradle $x:build -x test
    fi

  done
  mkdir -p $BUILDED_PATH
  rm -rf "$BUILDED_PATH/bin"
  cp -r bin "$BUILDED_PATH"
  chmod -R 755 "$BUILDED_PATH/"
fi

function pkgModule() {
  mn=$1
  newmn=$2
  if [ -d "$BUILDED_PATH/$newmn" ]; then
    rm -rf "$BUILDED_PATH/$newmn"
  fi
  mkdir -p $BUILDED_PATH/$newmn

  echo "Try make module path $BUILDED_PATH/$newmn"

  cd $PROJECT_PATH
  cd $mn

  echo "Try enter the path $PROJECT_PATH/$mn"

  if [ -d "$(pwd)/build" ]; then
    echo "copy $(pwd)/build/resources to $BUILDED_PATH/$newmn"
    cp -r build "$BUILDED_PATH/$newmn"
    rm -rf "$BUILDED_PATH/$newmn/build/classes"
    rm -rf "$BUILDED_PATH/$newmn/build/tmp"
    rm -rf "$BUILDED_PATH/$newmn/build/libs"
    mkdir -p $BUILDED_PATH/$newmn/build/libs
    echo "copy $(pwd)/build/libs/*.jar to $BUILDED_PATH/$newmn"
    cp build/libs/*.jar $BUILDED_PATH/$newmn/build/libs
  fi
  echo "$mn is done"
  cd ../
  if [ -n "$COMPRESS" ]; then
    cd $BUILDED_PATH/
    echo "$(pwd)"
    echo "tar -czvf ${newmn}.tar.gz ${newmn}"
    tar -czvf ${newmn}.tar.gz ${newmn}
    cd $PROJECT_PATH
  fi
}

for key in ${!map[*]}; do
  echo ${map[$key]}
done

for project in ${modules[*]}; do
  if [ -z ${map[$project]} ]; then
    pkgModule $project $project
  else
    pkgModule $project ${map[$project]}
  fi
done

echo 'done'
cd $runDir
