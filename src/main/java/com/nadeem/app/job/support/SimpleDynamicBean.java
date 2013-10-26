package com.nadeem.app.job.support;

import java.io.Serializable;

public class SimpleDynamicBean implements Serializable
{

    private static final long serialVersionUID = 1L;

    public void sayHello(String someBody)
    {
        System.out.println("Hello " + someBody + "!");
    }
}
