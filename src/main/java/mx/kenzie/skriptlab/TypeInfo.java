package mx.kenzie.skriptlab;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;

public record TypeInfo(Class<?> type, String name, String... patterns) implements Registered {
    
    @Override
    public void register() {
        Classes.registerClass(new ClassInfo<>(type, name).user(patterns));
    }
    
}
