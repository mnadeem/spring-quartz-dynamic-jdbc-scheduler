package com.nadeem.app.support;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest
{

    @Mock
    private ClassPathXmlApplicationContext context;

    @Before
    public void doBeforeEachTest()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSuccessfullyGetBean()
    {
        Whitebox.setInternalState(ApplicationContext.class, context);
        ApplicationContext.getBean(Mockito.anyString());
        Mockito.verify(this.context).getBean(Mockito.anyString());
    }

    @Test
    public void shouldSuccessfullyStartTheSystem()
    {
        Whitebox.setInternalState(ApplicationContext.class, context);
        ApplicationContext.start();
        Mockito.verifyZeroInteractions(this.context);
    }

    @Test
    public void shouldSuccessfullyShutDownTheSystem()
    {
        Whitebox.setInternalState(ApplicationContext.class, context);
        ApplicationContext.shutdown();
        Mockito.verify(this.context).close();
    }

    @Test
    public void shouldSuccessfullyShutDownTheSystemWithNullContext()
    {
        ApplicationContext.shutdown();
        Mockito.verifyZeroInteractions(this.context);
    }

    @Test
    public void testPrivateConstructor() throws Exception
    {
        ApplicationContext instance = Whitebox.invokeConstructor(ApplicationContext.class);
        assertNotNull(instance);
    }
}
