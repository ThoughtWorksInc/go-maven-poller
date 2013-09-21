package maven;

import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ModelTest {
    @Test
    public void testGetTrackbackURL() throws Exception {
        Model model = Model.unmarshal(new InputSource(new FileInputStream("test/fast/sample-pom.xml")));
        assertThat(model.getUrl(), is("http://www.jboss.org/abc/def"));
    }
}
