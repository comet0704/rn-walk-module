import AsyncStorage from "@react-native-async-storage/async-storage";
import React, { useEffect, useRef, useState } from "react";
import {
  AppState,
  BackHandler,
  NativeModules,
  Platform,
  SafeAreaView,
  View
} from "react-native";
import BackgroundService from "react-native-background-actions";
import StepCounter from "./utils/StepCounter";
import { Text, NativeEventEmitter } from "react-native";

// 백그라운드 서비스 옵션
const manbogiBackgroundServiceOptions = {
  taskName: "AlluluManbogiTask",
  taskTitle: "올웨이즈 올룰루",
  taskDesc: "걸음 수 측정 중...",
  taskIcon: {
    name: "ic_launcher_round",
    type: "mipmap",
  },
  color: "#ff00ff",
  linkingURI: "alwayz://appLink/?shareType=Manbogi",
  parameters: {
    delay: 1000,
  },
};

const AlluluScreen = ({ navigation }) => {
  const appState = useRef(AppState.currentState);
  const stepCount = useRef(0);
  const [streaming, setStreaming] = useState(false);
  const PedometerUtilModule = NativeModules.PedometerUtil; // Android PedometerUtil
  const ScheduleExactAlarm = NativeModules.ScheduleExactAlarm; // Android ScheduleExactAlarm Permission
  const [currentDate, setCurrentDate] = useState(null);
  // 현재 날짜 가져오는 함수
  const getCurrentDate = async () => {
    if (currentDate) {
      return currentDate;
    }
  };

  const getStepLogJson = async () => {
    const currentDate = await getCurrentDate();
    const today = new Date(currentDate?.data);

    today.setDate(today.getDate() - 1); // 하루 전
    today.setHours(0, 0, 0, 0);
    const start = new Date(today.getTime());

    start.setDate(start.getDate() - 2); // 3일 이전
    start.setHours(0, 0, 0, 0);

    const stepLog = [];

    try {
      // 3일 간격으로 각 날짜별 걸음수를 쿼리
      for (let date = start; date <= today; date.setDate(date.getDate() + 1)) {
        const startDate = new Date(date.getTime());
        startDate.setDate(startDate.getDate());
        const endDate = new Date(date.getTime());
        endDate.setDate(endDate.getDate() + 1);
        const data = await StepCounter.queryPedometerDataBetweenDatesAsync(
          startDate.getTime(),
          endDate.getTime()
        );

        const steps = data?.numberOfSteps ?? 0;
        stepLog.push({ stepsAt: data?.startDate, steps });
      }
    } catch (err) {
      console.log("Error retrieving step log:", err);
    }

    const stepLogJson = JSON.stringify(stepLog); // JSON 형태로 변환
    return stepLog;
  };

  
  //앱이 처음설치된 후 로그인 한다음 유저의 현재걸음수 바로 표시되게 
  const setStepFirstAppInstall = async () => {
    const currentStep = 300;
    PedometerUtilModule.setCurrentStep(currentStep);
  }

  // 오늘 걸음수 얻기
  const getTodayStep = async () => {
    const startDate = new Date();

    if (Platform.OS === "ios") {
      startDate.setHours(0, 0, 0, 0);
      const endDate = new Date();
      StepCounter.queryPedometerDataBetweenDates(
        startDate.getTime(), // react native can't pass Date object, so you should pass timestamp.
        endDate.getTime(),
        async (error, data) => {
          const steps = data?.numberOfSteps ?? 0;
          stepCount.current = steps;
          setTimeout(() => {
            if (BackgroundService.isRunning()) {
              BackgroundService.updateNotification({
                taskDesc: `👟 ${steps} 걸음`,
              });
            }
          }, 500);
        }
      );
    }
    if (Platform.OS === "android") {
      
      StepCounter.todayCurrentStep(
        async (error, data) => {
          const steps = data?.numberOfSteps ?? "";
          console.log("----current step----", steps);
        }
      );
    }
  };

  // 처음 로딩되었을 때 걸음수를 가져오는 함수
  const gettingStepCount = async () => {
    const startDate = new Date();

    if (Platform.OS === "ios") {
      startDate.setHours(0, 0, 0, 0);
      const endDate = new Date();
      StepCounter.queryPedometerDataBetweenDates(
        startDate.getTime(), // react native can't pass Date object, so you should pass timestamp.
        endDate.getTime(),
        async (error, data) => {
          const steps = data?.numberOfSteps ?? 0;
          stepCount.current = steps;
          setTimeout(() => {
            if (BackgroundService.isRunning()) {
              BackgroundService.updateNotification({
                taskDesc: `👟 ${steps} 걸음`,
              });
            }
          }, 500);
        }
      );
    }
    if (Platform.OS === "android") {
      startDate.setHours(12, 0, 0, 0);
      const endDate = new Date();
      endDate.setHours(12, 0, 0, 0);
      StepCounter.queryPedometerDataBetweenDates(
        startDate.getTime(),
        endDate.getTime(),

        async (error, data) => {
          const steps = data?.numberOfSteps ?? 0;
          stepCount.current = steps;

          setTimeout(() => {
            if (BackgroundService.isRunning()) {
              BackgroundService.updateNotification({
                taskDesc: `👟 ${steps} 걸음`,
              });
            }
          }, 500);
        }
      );
    }
  };

  // 걸음수 센서의 변화를 감지하고 실시간으로 걸음수를 가져오는 함수
  const trackingStep = async () => {
    const startDate = new Date();

    if (Platform.OS === "android") {
      startDate.setHours(23, 59, 59, 59);
      return;
    }
    if (Platform.OS === "ios") {
      startDate.setHours(0, 0, 0, 0);
    }

    const pedometerDataCallback = async (pedometerData) => {
      const steps = pedometerData?.numberOfSteps;

      stepCount.current = steps;

      if (BackgroundService.isRunning()) {
        BackgroundService.updateNotification({
          taskDesc: `👟 ${steps} 걸음`,
        });
      }
    };

    StepCounter.startPedometerUpdatesFromDate(
      startDate.getTime(),
      pedometerDataCallback
    );
  };

  const backgroundInit = async () => {
    await new Promise(() => {
      BackgroundService.updateNotification({
        taskDesc: `👟 ${stepCount.current} 걸음`,
      });
    })
      .then(() => console.log("bg init"))
      .catch((err) => console.log(err));
  };

  // 실시간 걸음수 측정을 위해 걸음수 센서를 시작하는 함수
  useEffect(() => {
    if (streaming) {
      trackingStep();
    }
  }, [streaming]);

  const startAllulu = async () => {
    if (Platform.OS === "android") {
     
     
    } else if (Platform.OS === "ios") {
      await StepCounter.requestPermission();
    }
    await setStreaming(true);
    await gettingStepCount();
    await getStepLogJson();

  };

  async function handleAppStateChange() {
    const subscription = AppState.addEventListener("change", (nextAppState) => {
      if (
        (Platform.OS !== "android" || Platform.Version <= 33) &&
        appState.current.match(/inactive|background/) &&
        nextAppState === "active"
      ) {
        startAllulu();
      }
      appState.current = nextAppState;
    });

    return subscription;
  }

  useEffect(() => {
    const subscription = handleAppStateChange();
    return () => {
      subscription.then((s) => s.remove());
    };
  }, []);


  const backAction = () => {
    return true;
  };

  useEffect(() => {
    loadBackAction();
    return () => {
      unLoadBackAction();
    };
  }, []);

  const loadBackAction = () => {
    BackHandler.addEventListener("hardwareBackPress", backAction);
  };

  const unLoadBackAction = () => {
    BackHandler.removeEventListener("hardwareBackPress", backAction);
  };

  useEffect(() => {
    setTimeout(() => {
      PedometerUtilModule.startNativeService();
    }, 500);

    
    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    let eventListener = eventEmitter.addListener('calledFromNative', event => {
      console.log(event.msg) // "someValue"

    });

    // Removes the listener once unmounted
    return () => {
      eventListener.remove();
    };
  }, []);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <View style={{ flex: 1 }}>
        <Text>AlluScreen</Text>
      </View>
    </SafeAreaView>
  );
};

export default AlluluScreen;
