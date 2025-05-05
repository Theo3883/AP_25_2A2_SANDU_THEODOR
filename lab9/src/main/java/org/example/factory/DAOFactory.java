package org.example.factory;

import org.example.dao.CityDAO;
import org.example.dao.CountryDAO;
import org.example.dao.impl.jdbc.JDBCCityDAO;
import org.example.dao.impl.jdbc.JDBCCountryDAO;
import org.example.dao.impl.jpa.JPACityDAO;
import org.example.dao.impl.jpa.JPACountryDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DAOFactory {
    private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);
    private static final String DAO_TYPE_PROPERTY = "dao.type";
    private static final String PROPERTIES_FILE = "dao.properties";
    private static String daoType;

    static {
        loadDaoType();
    }

    private static void loadDaoType() {
        Properties properties = new Properties();
        try (InputStream inputStream = DAOFactory.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                daoType = properties.getProperty(DAO_TYPE_PROPERTY, "JPA");
                logger.info("DAO type loaded from properties: {}", daoType);
            } else {
                logger.warn("Could not find {} file, defaulting to JPA", PROPERTIES_FILE);
                daoType = "JPA";
            }
        } catch (IOException e) {
            logger.error("Error loading DAO properties: {}", e.getMessage(), e);
            daoType = "JPA";
        }
    }

    public static CityDAO getCityDAO() {
        logger.debug("Creating {} implementation of CityDAO", daoType);
        if ("JDBC".equalsIgnoreCase(daoType)) {
            return new JDBCCityDAO();
        } else {
            return new JPACityDAO();
        }
    }

    public static CountryDAO getCountryDAO() {
        logger.debug("Creating {} implementation of CountryDAO", daoType);
        if ("JDBC".equalsIgnoreCase(daoType)) {
            return new JDBCCountryDAO();
        } else {
            return new JPACountryDAO();
        }
    }
}