package maven;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MavenVersionTest {
    @Test
    public void testVersionLabel() {
        String label = "1.2.3.4.yyy.xxx.qualifier";
        MavenVersion v = new MavenVersion(label);
        v.setArtifactId("a");
        v.setGroupId("g");
        assertThat(v.getV_Q(), is(label));
        assertThat(v.getRevisionLabel(), is("g:a."+label));
    }
}
