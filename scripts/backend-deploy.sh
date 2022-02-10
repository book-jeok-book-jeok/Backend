#!/bin/bash

BE_DIR=/home/ubuntu/S06P12A305/backend
BE_BUILD_DIR=$BE_DIR/build/libs

echo "########################"
echo "### 백엔드 배포 시작 ###"
echo "########################"
echo ""

echo "######################"
echo "# gradlew build 시작 #"
echo "######################"
cd $BE_DIR
./gradlew build
echo "######################"
echo "# gradlew build 종료 #"
echo "######################"
echo ""

echo "###########################################"
echo "# 실행 중인 스프링 부트 애플리케이션 중지 #"
echo "###########################################"
CURRENT_PID=$(pgrep -fl bjbj | grep java | awk '{print $1}')
if [ -z "$CURRENT_PID" ]; then
        echo "#######################################################"
        echo "# 현재 실행 중인 스프링 부트 애플리케이션은 없습니다. #"
        echo "#######################################################"
else
        echo "#########################################################"
        echo "# 현재 실행 중인 스프링 부트 애플리케이션 포트 : $CURRENT_PID #"
        echo "#########################################################"
        kill -15 $CURRENT_PID
        sleep 5
        echo "################################################"
        echo "# 실행 중인 스프링 부트 애플리케이션 중지 성공 #"
        echo "################################################"
fi
echo ""

echo "#################################"
echo "# 스프링 부트 애플리케이션 실행 #"
echo "#################################"
JAR_NAME="bjbj-0.0.1-SNAPSHOT.jar"
cd $BE_BUILD_DIR
nohup java -jar $JAR_NAME > ./nohup.out 2>&1 &
echo "######################################"
echo "# 스프링 부트 애플리케이션 실행 성공 #"
echo "######################################"
echo ""

echo "########################"
echo "### 백엔드 배포 종료 ###"
echo "########################"
echo ""