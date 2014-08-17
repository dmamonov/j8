package org.xgame.context;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-17 12:25 AM
 */
class State {
    static final ThreadLocal<ArrayList<State>> stackThreadLocal = new ThreadLocal<ArrayList<State>>(){
        @Override
        protected ArrayList<State> initialValue() {
            return new ArrayList<>(16);
        }
    };

    static State present(){
        final ArrayList<State> stack = stackThreadLocal.get();
        return stack.get(stack.size() - 1);
    }

    final State root;
    final Set<State> nestedSet = new HashSet<>(); //make optional.

    //=== domain ===
    static class DomainValue extends HashMap<String, Object>{
        static final ImmutableMap<Class, Object> defaultValues = ImmutableMap.<Class, Object>builder()
                .put(Integer.TYPE, 0)
                .put(Boolean.TYPE, false)
                .put(Double.TYPE, 0.0)
                .put(String.class, "")
                .build();
    }

    interface GetDomainValue {
        DomainValue _getDomainValue();
    }
    final Map<Class<? extends Entity.Data>, DomainValue> domain = new HashMap<>();
    Class<? extends Entity.Data> lastDataType = null;
    String lastDataField = null;

    //=== actions ===
    final Map<Class<? extends Entity.Action>, LinkedHashSet<Object>> activity = new HashMap<>();
    void addActivity(final Class<? extends Entity.Action> actionType, final Entity.Action actionInstance){
        final LinkedHashSet<Object> actionInstanceSet = activity.get(actionType);
        if (actionInstanceSet!=null) {
            actionInstanceSet.add(actionInstance);
        } else {
            final LinkedHashSet<Object> newActionInstanceSet = new LinkedHashSet<>();
            newActionInstanceSet.add(actionInstance);
            activity.put(actionType, newActionInstanceSet);
        }
    }
    //=== meta data ===
    boolean disposed = false;

    //=== stack ===
    State(final State root) {
        if (root!=null){
            this.root = root;
        } else {
            this.root = this;
        }
        this.root.nestedSet.add(this);
    }

    static Entity.Ref pushNew(final Entity.Configurer configurer) {
        stackThreadLocal.get().add(new State(present().root));
        try {
            return configurer.apply(Iam.entity);
        } finally {
            pop();
        }
    }

    private static void pop(){
        final ArrayList<State> stack = stackThreadLocal.get();
        stack.remove(stack.size() - 1);
    }

    static void push(final State state, final Entity.Handler handler) {
        checkNotNull(state);
        stackThreadLocal.get().add(state);
        try {
            handler.handle(Iam.entity);
        } finally {
            pop();
        }
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder("Entity:\n");
        for (final Map.Entry<Class<? extends Entity.Data>, DomainValue> domainEntry : domain.entrySet()) {
            if (domainEntry.getKey()==null){
                System.out.println("oops");
            }
            result.append("  $").append(domainEntry.getKey().getSimpleName()).append("(");
            boolean first = true;
            for (final Map.Entry<String, Object> field : domainEntry.getValue().entrySet()) {
                if (first){
                    first = false;
                } else {
                    result.append(", ");
                }
                result.append(field.getKey()).append(": ").append(field.getValue());
            }
            result.append(")\n");

        }
        for (final Class<? extends Entity.Action> actionType : activity.keySet()) {
            result.append("  #").append(actionType.getSimpleName()).append("\n");
        }

        return result.toString();
    }
}
