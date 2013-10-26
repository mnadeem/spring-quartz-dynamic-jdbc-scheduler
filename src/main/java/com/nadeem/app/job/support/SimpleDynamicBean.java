package com.nadeem.app.job.support;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDynamicBean implements Serializable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDynamicBean.class);
    private static final long serialVersionUID = 1L;

    public void sayHello(String someBody)
    {
        LOGGER.info("Hello " + someBody + "!");
    }
}
