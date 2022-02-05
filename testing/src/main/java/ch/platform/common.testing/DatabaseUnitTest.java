/*=============================================================================
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Che-Hung Lin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *===========================================================================*/

package ch.platform.common.testing;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ch.platform.common.dao.BaseDaoImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

public class DatabaseUnitTest {
    protected static EntityManager em;

    @BeforeAll
    public static void startupDatabase() {
        try {
            if (null == em) {
                Map<String, String> properties = new HashMap<>();
                properties.put("hibernate.show_sql", "false");
                properties.put("hibernate.connection.provider_class", "org.hibernate.connection.DriverManagerConnectionProvider");
                properties.put("hibernate.connection.url", "jdbc:hsqldb:mem:unit;sql.syntax_mys=true");
                properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
                properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
                properties.put("hibernate.connection.username", "sa");
                properties.put("hibernate.connection.password", "");
                properties.put("hibernate.cache.use_second_level_cache", "false");
                properties.put("hibernate.cache.use_query_cache", "false");
                properties.put("hibernate.hbm2ddl.auto", "create-drop");
                properties.put("javax.persistence.schema-generation.database.action", "drop-and-create");
                EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("persistence_test", properties);
                em = entityManagerFactory.createEntityManager();
                BaseDaoImpl.entityManagerOverride = em;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void beginTransaction() {
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE SCHEMA public AND COMMIT ").executeUpdate();
    }

    @AfterEach
    public void rollbackTransaction() {
        em.getTransaction().rollback();
    }

    protected void commitTransaction() {
        em.getTransaction().commit();
    }

    protected void commitTransaction(boolean clearContext) {
        commitTransaction();
        if (clearContext) {
            em.clear();
        }
    }

}