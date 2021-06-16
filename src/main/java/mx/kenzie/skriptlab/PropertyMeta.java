package mx.kenzie.skriptlab;

import ch.njol.skript.classes.Changer;

public interface PropertyMeta {
    
    Class<?> getType(Changer.ChangeMode mode);
    
}
