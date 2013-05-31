package ut.com.pawelniewiadomski.jira.openid.authentication;

import org.junit.Test;
import com.pawelniewiadomski.jira.openid.authentication.MyPluginComponent;
import com.pawelniewiadomski.jira.openid.authentication.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}