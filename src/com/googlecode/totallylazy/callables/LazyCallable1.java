package com.googlecode.totallylazy.callables;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Memory;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Closeables.safeClose;

public final class LazyCallable1<T, R> extends Function1<T, R> implements Memory {
    private final Callable1<? super T, ? extends R> callable;
    private final Map<T, R> state = new HashMap<T, R>();
    private final Object lock = new Object();

    private LazyCallable1(Callable1<? super T, ? extends R> callable) {
        this.callable = callable;
    }

    public static <T, R> LazyCallable1<T, R> lazy(Callable1<? super T, ? extends R> callable) {
        return new LazyCallable1<T, R>(callable);
    }

    public final R call(T instance) throws Exception {
        synchronized (lock) {
            if (!state.containsKey(instance)) {
                state.put(instance, callable.call(instance));
            }
            return state.get(instance);
        }
    }

    public void forget() {
        close();
    }

    @Override
    public void close() {
        synchronized (lock) {
            for (R r : state.values()) safeClose(r);
            state.clear();
        }
    }
}
