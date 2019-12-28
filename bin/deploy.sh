#!/bin/bash

//${base_dir}../deploy.sh -clean_history -build_path /home/deploy/build_btx-backend -project_path /home/deploy/btx-backend -deploy_path /var/www/gobtx -deploy_user deploy -module_file_name module.properties -module_file_path ${base_dir} -use_gradle_wrapper -module ws_server xchange xchange_frontend -ip 172.31.11.65 -version $VERSION -use_cache "$@"

echo "prepare to invoke the deploy action"

modules=()
depIps=()
DEP_USER="deploy"
RESERVED_CNT=5
MODULE_FILE_NAME="module"
USD_GRADLE_WRAPPER="FALSE"

while [ $# -gt 0 ]; do
  COMMAND=$1
  case $COMMAND in
  -module)
    ISBREAK="0"
    shift 1
    while [ $ISBREAK -eq "0" ] && [ $# -gt 0 ]; do
      ICOMMAND=$1
      echo "MODULE=>$ICOMMAND"
      echo "------"
      if [[ $ICOMMAND == -* ]]; then
        ISBREAK="1"
      else
        modules[${#modules[*]}]=$ICOMMAND
        shift 1
      fi
    done
    ;;
  -ip)
    ISBREAK2="0"
    shift 1
    while [ $ISBREAK2 -eq "0" ] && [ $# -gt 0 ]; do
      ICOMMAND2=$1
      echo "IP=>$ICOMMAND2"
      echo "------"
      if [[ $ICOMMAND2 == -* ]]; then
        ISBREAK2="1"
      else
        depIps[${#depIps[*]}]=$ICOMMAND2
        shift 1
      fi
    done
    ;;
  -project_path)
    PROJECT_PATH=$2
    shift 2
    ;;
  -deploy_path)
    DEP_PATH=$2
    shift 2
    ;;
  -version)
    VERSION=$2
    shift 2
    ;;
  -deploy_user)
    DEP_USER=$2
    shift 2
    ;;
  -build_path)
    BUILDED_PATH=$2
    shift 2
    ;;
  -build_all)
    BUILD_ALL="TRUE"
    shift
    ;;
  -use_cache)
    USE_CACHE="TRUE"
    shift
    ;;
  -reserved_cnt)
    RESERVED_CNT=$2
    shift 2
    ;;
  -clean_history)
    CLEAN_HISTORY="TRUE"
    shift
    ;;
  -module_file_name)
    MODULE_FILE_NAME=$2
    shift 2
    ;;
  -module_file_path)
    MODULE_FILE_PATH=$2
    shift 2
    ;;
  -use_gradle_wrapper)
    USD_GRADLE_WRAPPER="TRUE"
    shift 1
    ;;
  *)
    break
    ;;
  esac
done

if [ -z "$MODULE_FILE_PATH" ]; then
  MODULE_FILE_PATH=$PROJECT_PATH
fi

echo "project_path = $PROJECT_PATH"
echo "builded_path = $BUILDED_PATH"
echo "deploy_user = $DEP_USER"
echo "deploy_path = $DEP_PATH"

MODULE_FILE="$MODULE_FILE_PATH${MODULE_FILE_NAME}"

echo "Prepare pop the module properties : ${MODULE_FILE}"

declare -A map

INDEX=0
for kv in $(grep -P -o "[^=\s]+\s*=\s*[^=\s]+" ${MODULE_FILE} | sed s/[[:space:]]//g); do
  OLD_IFS="$IFS"
  IFS="="
  TARR=($kv)
  IFS="$OLD_IFS"
  echo "${TARR[0]} => ${TARR[1]}"
  map[${TARR[0]}]=${TARR[1]}
  ((INDEX = INDEX + 1))

done

#runDir=`pwd`

runDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

echo "build.sh path ${DIR}"

buildModules=""
sp=""

for module in ${modules[@]}; do
  buildModules=$buildModules$sp$module
  sp=","
done

echo "build $buildModules"


echo "sh $PROJECT_PATH/build.sh"

sh $PROJECT_PATH/build.sh

#if [ "$USE_CACHE" != "TRUE" ]; then
#  if [ "$BUILD_ALL" == "TRUE" ]; then
#    sh $runDir/build.sh -project_path $PROJECT_PATH -builded_path $BUILDED_PATH -module_file_name $MODULE_FILE_NAME -module_file_path $MODULE_FILE_PATH -use_gradle_wrapper $USD_GRADLE_WRAPPER
#  else
#    sh $runDir/build.sh -build $buildModules -project_path $PROJECT_PATH -builded_path $BUILDED_PATH -module_file_name $MODULE_FILE_NAME -module_file_path $MODULE_FILE_PATH -use_gradle_wrapper $USD_GRADLE_WRAPPER
#  fi
#fi

echo "build result  $? , if not ZERO this may doom us!!"

cd $BUILDED_PATH

echo "go to build path  ${BUILDED_PATH}"

if [ -z "$VERSION" ]; then
  VERSION="$(date +%Y%m%d%H%M%S)"
fi

F_DEP_PATH="$DEP_PATH/$VERSION"
S_DEP_PATH="$DEP_PATH/current"

for depIp in ${depIps[@]}; do
  #ssh root@$depIp "mkdir -p $DEP_PATH;chown $DEP_USER $DEP_PATH;"
  echo "Try SSH  $DEP_USER@$depIp mkdir -p $F_DEP_PATH"

  ssh $DEP_USER@$depIp "mkdir -p $F_DEP_PATH"
  scp -r -q bin $DEP_USER@$depIp:$F_DEP_PATH
  for module in ${modules[@]}; do
    moduleAlias=${map[$module]}
    echo "scp -r $moduleAlias $DEP_USER@$depIp:$F_DEP_PATH"
    scp -r -q $moduleAlias $DEP_USER@$depIp:$F_DEP_PATH

    echo "deploy ${moduleAlias} done"
  done
  echo "ssh $DEP_USER@$depIp 'ln -s $F_DEP_PATH $S_DEP_PATH'"
  ssh $DEP_USER@$depIp "rm -rf $S_DEP_PATH"
  ssh $DEP_USER@$depIp "ln -s $F_DEP_PATH $S_DEP_PATH"
done

if [ "x$CLEAN_HISTORY" == "xTRUE" ]; then

  index=0
  for file in $(ssh $DEP_USER@$depIp "ls -lt $DEP_PATH" | grep ^d | awk '{print $9}' | grep -P ^[0-9]{14}); do
    filelist[$index]=$file
    ((index++))
  done

  for i in ${!filelist[@]}; do
    if [[ $i -ge $RESERVED_CNT ]]; then
      echo "remove the expired version => $DEP_PATH/${filelist[$i]}"
      ssh $DEP_USER@$depIp "rm -rf $DEP_PATH/${filelist[$i]}"
    fi
  done

fi
