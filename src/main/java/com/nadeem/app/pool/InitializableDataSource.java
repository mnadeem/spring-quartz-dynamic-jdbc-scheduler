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

/**
 * This Datasource has the capability to execute custom scripts on application startup, 
 * @author nadeem
 *
 */
public class InitializableDataSource extends BasicDataSource
{
    private List<String> initScripts                          = new ArrayList<String>();
    private final ResourceLoader resourceLoader               = new DefaultResourceLoader();
    private final ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

    public void init()
    {
        addScriptsToPopulator();
        populateDataBase();
    }

    private void addScriptsToPopulator()
    {
        for (String sqlResource : getInitScripts())
        {
            this.databasePopulator.addScript(this.resourceLoader.getResource(sqlResource));
        }
    }

    private void populateDataBase()
    {
        try
        {
            doPopulate(getConnection());
        }
        catch (SQLException ex)
        {
            throw new DataAccessResourceFailureException("Failed to populate database", ex);
        }
    }

    private void doPopulate(final Connection connection) throws SQLException
    {
        try
        {
            this.databasePopulator.populate(connection);
        }
        finally
        {
            closeQuitely(connection);
        }
    }

    private void closeQuitely(final Connection connection)
    {
        try
        {
            connection.close();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    private List<String> getInitScripts()
    {
        return this.initScripts;
    }

    public void setInitScripts(final List<String> newInitScripts)
    {
        this.initScripts = newInitScripts;
    }
}
