package com.nadeem.app.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public class InitializableDataSource extends BasicDataSource
{
    private List<String> intScripts = new ArrayList<String>();
    private ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    public void init()
    {

        for (String sqlResource : intScripts)
        {
            databasePopulator.addScript(this.resourceLoader.getResource(sqlResource));
        }

        try
        {
            Connection connection = getConnection();
            try
            {
                this.databasePopulator.populate(connection);
            }
            finally
            {
                try
                {
                    connection.close();
                }
                catch (SQLException ex)
                {
                    // ignore
                }
            }
        }
        catch (SQLException ex)
        {
            throw new DataAccessResourceFailureException("Failed to populate database", ex);
        }
    }

    public List<String> getIntScripts()
    {
        return intScripts;
    }

    public void setIntScripts(List<String> intScripts)
    {
        this.intScripts = intScripts;
    }
}
