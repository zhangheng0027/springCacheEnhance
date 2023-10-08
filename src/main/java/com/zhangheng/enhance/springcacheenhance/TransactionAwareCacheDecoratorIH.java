package com.zhangheng.enhance.springcacheenhance;

import lombok.SneakyThrows;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class TransactionAwareCacheDecoratorIH extends TransactionAwareCacheDecorator {

    private static final ThreadLocal<Map<Object, TCache>> transactionCache = new ThreadLocal<>();

    public TransactionAwareCacheDecoratorIH(Cache targetCache) {
        super(targetCache);
    }

    protected static void init() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            Map<Object, TCache> map = transactionCache.get();
            if (map == null) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        transactionCache.remove();
                    }
                });
                map = new HashMap<>();
                transactionCache.set(map);
            }
        }
    }

    protected static TCache getTransactionCache(String key) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            init();
            return transactionCache.get().get(key);
        }
        return null;
    }

    protected void putInTransactionCache(Object key, String op, Object value) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            init();
            TCache tCache = new TCache(op);
            tCache.setValue(value);
            transactionCache.get().put(key, tCache);
        }
    }

    protected void clearTransactionCache() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            init();
            transactionCache.get().clear();
            transactionCache.get().put("clear::" + this.getName(), new TCache("clear"));
        }
    }


    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        TCache byInTransactionCache = getTransactionCache(String.valueOf(key));
        if (null == byInTransactionCache)
            return super.get(key);
        if ("put".equals(byInTransactionCache.getType()))
            return new SimpleValueWrapper(byInTransactionCache.getValue());
        return null;
    }

    @Override
    public <T> T get(Object key, @Nullable Class<T> type) {
        ValueWrapper valueWrapper = get(key);
        if (null == valueWrapper)
            return null;
        return (T) valueWrapper.get();
    }

    @Override
    @Nullable
    @SneakyThrows
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper valueWrapper = get(key);
        if (null != valueWrapper)
            return (T) valueWrapper.get();
        return valueLoader.call();
    }

    @Override
    public void put(final Object key, @Nullable final Object value) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            putInTransactionCache(key, "put", value);
        }
        super.put(key, value);
    }

    @Override
    public void evict(final Object key) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            putInTransactionCache(key, "evict", null);
        }
        super.evict(key);
    }

    @Override
    public void clear() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            clearTransactionCache();
        }
        super.clear();
    }

    static public class TCache {

        /** 操作类型 **/
        String type;

        Object value;

        TCache(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

}
