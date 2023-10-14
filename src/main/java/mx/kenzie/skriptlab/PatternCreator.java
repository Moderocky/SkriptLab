package mx.kenzie.skriptlab;

public class PatternCreator {
    
    protected final String input;
    protected String[] words;
    
    
    public PatternCreator(String input) {
        this.input = input.trim();
    }
    
    public boolean unravel() {
        if (words != null) return false;
        this.words = input.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        return true;
    }
    
    public String getPattern() {
        this.unravel();
        return String.join(" ", words).toLowerCase();
    }
    
    public String[] getWords() {
        this.unravel();
        return words;
    }
    
    public String getInput() {
        return input;
    }
    
}
