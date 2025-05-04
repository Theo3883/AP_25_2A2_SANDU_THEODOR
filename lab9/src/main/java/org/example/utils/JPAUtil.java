package org.example.utils;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {
    private static EntityManagerFactory emf;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {

            Dotenv dotenv = Dotenv.load();

            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", dotenv.get("DB_URL"));
            props.put("jakarta.persistence.jdbc.user", dotenv.get("DB_USER"));
            props.put("jakarta.persistence.jdbc.password", dotenv.get("DB_PASSWORD"));

            emf = Persistence.createEntityManagerFactory("citydb", props);
        }
        return emf;
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}