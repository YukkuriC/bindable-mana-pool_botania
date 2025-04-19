package io.yukkuric.bindable_mana_pool;

public abstract class BindableManaPool {
    public static final String MOD_ID = "bindable_mana_pool";
    static BindableManaPool INSTANCE;

    public BindableManaPool() {
        INSTANCE = this;
    }
}
