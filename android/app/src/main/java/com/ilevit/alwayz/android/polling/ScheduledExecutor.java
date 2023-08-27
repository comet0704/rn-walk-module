package com.ilevit.alwayz.android.polling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutor {


    /**
     * 설명
     * 스레드풀의 내부의 스레드를 사용해 작업을 실행하는 방법을 사용하면, 작업별로 매번 스레드를 생성해 처리하는 방법보다는 굉장히 많은 장점이 있다.
     * 매번 스레드를 생성하는 대신 이전에 사용했던 스레드를 재사용하기 때문에 스레드를 계속해서 생성할 필요가 없고,
     * 따라서 여러개의 요청을 처리하는데 필요한 시스템 자원이 줄어드는 효과가 있다. 더군다나 클라이언트가 요청을 보냈을때 해당 요청을 처리할
     * 스레드가 이미 만들어진 상태로 대기 하고 있기 때문에 작업을 실행하는 데 딜레이가 발생하지 않아 전체적인 반응 속도도 향상된다.
     * 아래와 같이 다양한 newXXXX()를 제공하며, ExecutorService 또는 ScheduledExecutorService를 생성(new)하여 리턴한다.
     * <p>
     * newFixedThreadPool(int nThreads) : nThreads 개수만큼의 스레드를 항상 유지.일반적인 스레드풀
     * 항상 일정한 스레드 개수를 유지한다.
     * 스레드가 유휴상태이더라도 제거하지 않고 유지한다.
     * 다만 작업도중 비정상적으로 스레드가 종료하는 경우에는 스레드를 추가로 생성하며, nThreads 개수보다 1개가 더 생길 수도 있다.
     * <p>
     * newSingleThreadExecutor() : 스레드 1개만 사용. 순차처리용. newFixedThreadPool(1)과 비슷
     * 항상 1개의 스레드만 동작한다.
     * 따라서 스레드가 동작중일 경우 나머지 작업은 모두 큐에서 대기하며, 순서대로 하나씩 실행된다.
     * 만약 비정상적으로 스레드가 종료되는 경우, 새로 스레드를 생성하고 남은 작업을 계속 한다.
     * <p>
     * newCachedThreadPool() : 스레드 개수에 제한 없음. 짧게 처리되는 태스크에 적합.사용가능한 스레드가 없을때 추가로 스레드 생성. 60초동안 사용안하면 스레드 제거
     * 스레드 개수에 제한이 없이 필요한 경우 계속 스레드 수가 증가한다.
     * 다만 일정 시간(60초)동안 사용하지 않는(idle) 스레드는 종료된다.
     * 필요없는 스레드를 제거하므로 서버 리소스(memory)는 적게 사용하지만, 스레드 생성과 삭제를 반복하므로 작업 부하가 불규칙적인 경우 비효율적이다.
     * <p>
     * <p>
     * newScheduledThreadPool(int corePoolSize) : 주기적으로 반복 실행되는 태스크용
     * 일정 시간 이후에 실행되거나 주기적으로 작업을 실행할 수 있으며, 스레드의 수가 고정되어 있는 형태의 Executor.Timer 클래스의 기능과 유사하다
     * <p>
     * newSingleThreadScheduledExecutor(): newScheduledThreadPool(1)와 비슷
     */

    //    Runnable _runnable = null;
    ScheduledExecutorService _scheduledExecutorService = null;


    public ScheduledExecutor(int corePoolSize) {
        super();
        _scheduledExecutorService = Executors.newScheduledThreadPool(corePoolSize);
    }

    public boolean unschedule(ScheduledFuture<?> scheduledFuture) {
        if (scheduledFuture != null) {
            return scheduledFuture.cancel(true);
        }
        return false;
    }

    public ScheduledFuture<?> fScheduleAtFixedRate(Runnable r, long sTime, long eTime, TimeUnit unit) {
        return _scheduledExecutorService.scheduleAtFixedRate(r, sTime, eTime, unit);
    }

}
