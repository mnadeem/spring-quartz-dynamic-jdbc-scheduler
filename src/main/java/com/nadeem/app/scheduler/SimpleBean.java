package com.nadeem.app.scheduler;

import java.io.Serializable;

public class SimpleBean implements Serializable
{

    private static final long serialVersionUID = 1L;

    public void sayHello(String someBody)
    {
        System.out.println("Hello " + someBody + "!");
    }

}
