package mx.kenzie.skriptlab.template;

import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public interface GeneratedMember {
    
    String getSyntax();
    
    default List<Object> getConvertedExpressions(Event event) {
        return null;
    }
    
}
