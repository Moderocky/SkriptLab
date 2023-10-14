package mx.kenzie.skriptlab;

import org.junit.Test;

public class PatternCreatorTest {
    
    @Test
    public void unravel() {
        final PatternCreator creator = new PatternCreator("helloThere");
        assert creator.words == null;
        final boolean result = creator.unravel();
        assert result;
        assert creator.words != null;
        final boolean again = creator.unravel();
        assert !again;
    }
    
    @Test
    public void getPattern() {
        final PatternCreator creator = new PatternCreator("helloThere");
        creator.unravel();
        assert creator.getPattern().equals("hello there");
        assert new PatternCreator("helloThere").getPattern().equals("hello there");
        assert new PatternCreator("testPatternThing").getPattern().equals("test pattern thing");
        assert new PatternCreator("testHTTPThing").getPattern().equals("test http thing");
        assert new PatternCreator("testBLOB").getPattern().equals("test blob");
        assert new PatternCreator("EXPTest").getPattern().equals("exp test");
    }
    
    @Test
    public void getWords() {
        final PatternCreator creator = new PatternCreator("helloTESTThere");
        assert creator.words == null;
        creator.unravel();
        assert creator.words != null;
        assert creator.getWords().length == 3;
        assert creator.getWords()[0].equals("hello");
        assert creator.getWords()[1].equals("TEST");
        assert creator.getWords()[2].equals("There");
    }
    
    @Test
    public void getInput() {
        final PatternCreator creator = new PatternCreator("helloTESTThere");
        assert creator.input.equals("helloTESTThere");
    }
    
}
