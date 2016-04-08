package com.cqyuanye.dispatcher;

import java.util.concurrent.*;

/**
 * Created by yuanye on 2016/4/8.
 */
public class AsyncDispatcher {

    private final int HANDLE_THREAD_NUM = 5;

    private final BlockingQueue<Event> eventQueue = new LinkedBlockingDeque<>();
    private final Executor executor;
    private final ConcurrentHashMap<Class, EventHandler<Event>> handlerMap = new ConcurrentHashMap<>();


    public AsyncDispatcher() {
        executor = Executors.newFixedThreadPool(HANDLE_THREAD_NUM);
        for (int i = 0; i < HANDLE_THREAD_NUM; i++){
            executor.execute(new HandleThread());
        }
    }

    public void handle(Event e) {
        eventQueue.offer(e);
    }

    public void registerEventHandler(Class eventType, EventHandler<Event> handler) {
        handlerMap.put(eventType, handler);
    }

    private EventHandler<Event> getHandler(Class eventType){
        return handlerMap.get(eventType);
    }


    private class HandleThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Event event = eventQueue.poll(10, TimeUnit.SECONDS);
                    if (event != null){
                        EventHandler<Event> handler = getHandler(event.getType());
                        handler.handle(event);
                    }
                } catch (InterruptedException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }
            }
        }
    }
}
