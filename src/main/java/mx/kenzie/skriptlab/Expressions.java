package mx.kenzie.skriptlab;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
public class Expressions implements Collection<Expression<?>> {
    
    private final List<Expression<?>> expressions;
    private final Event event;
    
    protected Expressions(Event event, Expression<?>... expressions) {
        this.event = event;
        this.expressions = Arrays.asList(expressions);
    }
    
    public static Expressions of(Event event, Expression<?>... expressions) {
        return new Expressions(event, expressions);
    }
    
    public <Type> Expression<Type> getExpression(int index) {
        return (Expression<Type>) expressions.get(index);
    }
    
    public <Type> Type get(int index) {
        return (Type) expressions.get(index).getSingle(event);
    }
    
    public <Type> Type getOrDefault(int index, Type alternative) {
        final Optional<?> optional = expressions.get(index).getOptionalSingle(event);
        return optional.map(object -> (Type) object).orElse(alternative);
    }
    
    public <Type> Type[] getArray(int index) {
        
        return (Type[]) expressions.get(index).getArray(event);
    }
    
    public <Type> Type[] getAll(int index) {
        return (Type[]) expressions.get(index).getAll(event);
    }
    
    @Override
    public int size() {
        return expressions.size();
    }
    
    @Override
    public boolean isEmpty() {
        return expressions.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return expressions.contains(o);
    }
    
    @NotNull
    @Override
    public Iterator<Expression<?>> iterator() {
        return expressions.iterator();
    }
    
    @NotNull
    @Override
    public Expression<?> @NotNull [] toArray() {
        return expressions.toArray(new Expression[0]);
    }
    
    @NotNull
    @Override
    public <Type> Type @NotNull [] toArray(@NotNull Type @NotNull [] array) {
        return expressions.toArray(array);
    }
    
    @Override
    public boolean add(Expression<?> expression) {
        return false;
    }
    
    @Override
    public boolean remove(Object o) {
        return false;
    }
    
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(expressions).containsAll(c);
    }
    
    @Override
    public boolean addAll(@NotNull Collection<? extends Expression<?>> c) {
        return false;
    }
    
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }
    
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }
    
    @Override
    public void clear() {
    }
    
}
