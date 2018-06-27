package org.niantuo.rxretrofit;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


/**
 * Created by niantuo on 2016/10/29.
 * RxBus
 */

public class RxBus {

    private final String TAG = RxBus.class.getSimpleName();
    private static volatile RxBus rxBus;
    private int level = Log.DEBUG;

    public static RxBus getBus() {
        if (rxBus == null) {
            synchronized (RxBus.class) {
                if (rxBus == null) rxBus = new RxBus();
            }
        }
        return rxBus;
    }


    private final Map<Class<?>, Object> mStickyEventMap;
    private final Subject<Object> mBus;

    private RxBus() {
        mBus = PublishSubject.create();
        mStickyEventMap = new ConcurrentHashMap<>();
    }

    /**
     * 注册普通事件
     *
     * @param <T>
     * @param cls
     * @return
     */
    public <T> Observable<T> register(Class<T> cls) {
        if (level <= Log.DEBUG)
            Log.d(TAG, "register: " + cls.getName());
        return mBus.ofType(cls);
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     */
    public <T> Observable<T> registerSticky(final Class<T> eventType) {
        synchronized (mStickyEventMap) {
            Observable<T> observable = mBus.ofType(eventType);
            final Object event = mStickyEventMap.get(eventType);
            if (event != null) {
                return observable.mergeWith(Observable.just(eventType.cast(event)));
            } else {
                return observable;
            }
        }
    }


    /**
     * 发送普通事件
     *
     * @param event
     */
    public void post(Object event) {
        if (level <= Log.DEBUG)
            Log.d(TAG, "post: " + event);
        mBus.onNext(event);
    }

    /**
     * Stciky 相关
     */

    /**
     * 发送一个新Sticky事件
     */
    public void postSticky(Object event) {
        synchronized (mStickyEventMap) {
            mStickyEventMap.put(event.getClass(), event);
        }
        post(event);
    }

    /**
     * 根据eventType获取Sticky事件
     */
    public <T> T getStickyEvent(Class<T> eventType) {
        synchronized (mStickyEventMap) {
            return eventType.cast(mStickyEventMap.get(eventType));
        }
    }


    /**
     * 移除指定eventType的Sticky事件
     */
    public <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (mStickyEventMap) {
            return eventType.cast(mStickyEventMap.remove(eventType));
        }
    }

    /**
     * 移除所有的Sticky事件
     */
    public void removeAllStickyEvents() {
        synchronized (mStickyEventMap) {
            mStickyEventMap.clear();
        }
    }

    /**
     * 判断是否有订阅者
     */
    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    /**
     * 重置
     */
    public void reset() {
        rxBus = null;
    }

}
