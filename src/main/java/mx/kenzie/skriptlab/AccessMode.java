package mx.kenzie.skriptlab;

import ch.njol.skript.classes.Changer;

public enum AccessMode {
    
    GET(null, false, true),
    SET(Changer.ChangeMode.SET, true, false),
    ADD(Changer.ChangeMode.ADD, true, false),
    REMOVE(Changer.ChangeMode.REMOVE, false, false),
    RESET(Changer.ChangeMode.RESET, false, false),
    DELETE(Changer.ChangeMode.DELETE, false, false);
    public final Changer.ChangeMode mode;
    public final boolean expectArguments, expectReturn;
    
    AccessMode(Changer.ChangeMode mode, boolean expectArguments, boolean expectReturn) {
        this.mode = mode;
        this.expectArguments = expectArguments;
        this.expectReturn = expectReturn;
    }
    
}
