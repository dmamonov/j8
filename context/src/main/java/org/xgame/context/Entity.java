package org.xgame.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.*;
import static org.xgame.context.Iam.entity;
import static org.xgame.context.State.present;
import static org.xgame.context.impl.DefaultInstanceCache.defaultInterfaceInstance;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-16 10:18 PM
 */
public interface Entity {
    //=== data related operations ===
    interface Data {
    }

    default <D extends Data> D data(final Class<D> dataType){
        //noinspection unchecked
        return (D) Proxy.newProxyInstance(Entity.class.getClassLoader(), new Class[]{dataType}, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                checkArgument(args==null);
                final State state = present();
                state.lastDataType = dataType;
                state.lastDataField = method.getName();
                final State.DomainValue value = state.domain.get(dataType);
                final Object result = value != null ? value.get(method.getName()) : null;
                if (result != null) {
                    return result;
                } else {
                    @SuppressWarnings("UnnecessaryLocalVariable")
                    final Object defaultValue = checkNotNull(State.DomainValue.defaultValues.get(method.getReturnType()), method.getReturnType());
                    //value.put(method.getName(), defaultValue);
                    return defaultValue;
                }
            }
        });
    }

    default DataOperation change(){
        return Iam.dataOperation;
    }

    interface DataOperation {
        default <T> DataOperation assign(final T marker, final T value){
            final State state = present();
            checkNotNull(state.lastDataType);
            checkNotNull(state.lastDataField);

            final State.DomainValue domainValue;
            {
                final State.DomainValue storedValue = state.domain.get(state.lastDataType);
                if (storedValue!=null){
                    domainValue = storedValue;
                } else {
                    domainValue = new State.DomainValue();
                    state.domain.put(state.lastDataType, domainValue);
                }
            }
            domainValue.put(state.lastDataField, value);

            return this;
        }

        default <T extends Data> DataOperation assign(final Class<T> type, final T value){

            return this;
        }

        default DataOperation plus(final double value, final double add){
            return assign(value, value+add);
        }

        default DataOperation plus(final int value, final int add){
            return assign(value, value+add);
        }
    }

    //=== query related operations ===
    interface ActionHandler<A extends Action> {
        void handle(A action);
    }

    default <A extends Action> void query(final Class<A> actionType, final ActionHandler<A> handler){
        final State state = present();
        for (final State nested : state.nestedSet) {
            @SuppressWarnings({"unchecked"})
            final A action = (A) nested.activity.get(actionType);
            if (action!=null){
                State.push(nested, e->handler.handle(action));
            }
        }
    }

    //=== action related operations ===
    interface Action extends Entity {
    }

    interface ProxyAction extends Entity {
    }

    interface Configurer extends Function<Entity, Ref> {

    }

    interface Handler {
        void handle(Entity entity);
    }

    default <A extends Action> A action(final Class<A> actionType) {
        final A action = find(actionType);
        if (action!=null){
            return action;
        } else {
            throw new IllegalStateException("No action: " + actionType + " in: " + describe());
        }
    }

    default <A extends Action> A find(final Class<A> actionType){
        final State state = present();
        //noinspection unchecked
        return (A) state.activity.get(actionType);
    }

    default <A extends Action> Entity addAction(final Class<A> actionType){
        final State state = present();
        final A actionInstance = defaultInterfaceInstance(actionType);
        state.activity.put(actionType, actionInstance);
        System.out.println("Register action: " + actionType.getSimpleName());
        for (final Class<?> subActionProposal : actionType.getInterfaces()) {
            if (Action.class.isAssignableFrom(subActionProposal) && Action.class!=subActionProposal) {
                @SuppressWarnings("unchecked")
                final Class<? extends Action> subAction = (Class<? extends Action>) subActionProposal;
                System.out.println("  Also registered action: "+subAction.getSimpleName());
                state.activity.put(subAction, actionInstance); //implement list.
            }
        }


        return this;
    }

    default <A extends Action> Entity removeAction(final Class<A> actionType){
        final State state = present();
        state.activity.remove(actionType);
        return this;
    }

    //=== lifecycle ===
    interface Ref {
        void with(Handler handler);
    }

    default Ref self(){
        return new Ref() {
            private final State refState = present();

            @Override
            public void with(final Handler handler) {
                State.push(refState, handler);
            }
        };
    }
    default void dispose(){
        final State state = present();
        if (state == state.root) {
            final Set<State> disposedSet = new HashSet<>();
            for (final State nested : state.nestedSet) {
                if (nested.disposed) {
                    disposedSet.add(nested);
                }
            }
            state.nestedSet.removeAll(disposedSet);
        } else {
            state.disposed = true;
        }
    }

    interface Fabricator extends Entity {
        void fabric();
    }

    default Ref create(final Configurer handler){
        return State.pushNew(handler);
    }

    default Ref create(final Fabricator fabricator){
        return create(e->{fabricator.fabric(); return e.self();});
    }

    default String describe() {
        return present().toString();
    }

    default String describeOneLine() {
        return describe().replace('\n', ' ');
    }

    static void seed(final Handler seeder){
        final ArrayList<State> localStack = State.stackThreadLocal.get();
        checkState(localStack.isEmpty());
        localStack.add(new State(null)); //hide this logic in state class.
        seeder.handle(entity);
    }

}
