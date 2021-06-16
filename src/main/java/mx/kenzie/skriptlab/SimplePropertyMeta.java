package mx.kenzie.skriptlab;

import ch.njol.skript.classes.Changer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class SimplePropertyMeta implements PropertyMeta {
    
    final String syntax;
    Method get, add, set, remove, removeAll, delete, reset;
    
    SimplePropertyMeta(final String syntax) {
        this.syntax = syntax;
    }
    
    public Method getMethod(Changer.ChangeMode mode) {
        return switch (mode) {
            case ADD -> add;
            case SET -> set;
            case REMOVE -> remove;
            case REMOVE_ALL -> removeAll;
            case DELETE -> delete;
            case RESET -> reset;
        };
    }
    
    public void setHandler(mx.kenzie.skriptlab.Changer[] changers, Method method) {
        for (final mx.kenzie.skriptlab.Changer changer : changers) {
            this.setHandler(changer, method);
        }
    }
    
    public void setHandler(mx.kenzie.skriptlab.Changer changer, Method method) {
        switch (changer) {
            case SET -> set = method;
            case ADD -> add = method;
            case RESET -> reset = method;
            case REMOVE -> remove = method;
            case REMOVE_ALL -> removeAll = method;
            case DELETE -> delete = method;
        }
    }
    
    public Map<String, Method> getHandles() {
        final HashMap<String, Method> map = new HashMap<>();
        if (add != null) map.put("add", add);
        if (set != null) map.put("set", set);
        if (remove != null) map.put("remove", remove);
        if (removeAll != null) map.put("removeAll", removeAll);
        if (delete != null) map.put("delete", delete);
        if (reset != null) map.put("reset", reset);
        return map;
    }
    
    public Map<Changer.ChangeMode, Class<?>[]> getModeMap() {
        final HashMap<Changer.ChangeMode, Class<?>[]> map = new HashMap<>();
        if (add != null) map.put(Changer.ChangeMode.ADD, add.getParameterTypes());
        if (set != null) map.put(Changer.ChangeMode.SET, set.getParameterTypes());
        if (remove != null) map.put(Changer.ChangeMode.REMOVE, remove.getParameterTypes());
        if (removeAll != null) map.put(Changer.ChangeMode.REMOVE_ALL, removeAll.getParameterTypes());
        if (delete != null) map.put(Changer.ChangeMode.DELETE, get.getParameterTypes());
        if (reset != null) map.put(Changer.ChangeMode.RESET, get.getParameterTypes());
        return map;
    }
    
    public Class<?> getType() {
        return get.getReturnType();
    }
    
    @Override
    public Class<?> getType(Changer.ChangeMode mode) {
        return switch (mode) {
            case ADD -> add != null ? add.getReturnType() : null;
            case SET -> set != null ? set.getReturnType() : null;
            case REMOVE -> remove != null ? remove.getReturnType() : null;
            case REMOVE_ALL -> removeAll != null ? removeAll.getReturnType() : null;
            case DELETE -> delete != null ? delete.getReturnType() : null;
            case RESET -> reset != null ? reset.getReturnType() : null;
        };
    }
    
    public Class<?> getDeclaringClass() {
        return get.getDeclaringClass();
    }
}
