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

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.ProviderChecker;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicHibernatePersistenceProvider extends HibernatePersistenceProvider implements PersistenceProvider {

    private final List<Class<? extends Annotation>> entityAnnotationClasses;

    public DynamicHibernatePersistenceProvider() {
        entityAnnotationClasses = listEntityAnnotationClasses();
    }

    private static List<Class<? extends Annotation>> listEntityAnnotationClasses() {
        final List<Class<? extends Annotation>> result = new ArrayList<>(2);
        result.add(MappedSuperclass.class);
        result.add(Entity.class);
        return result;
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor, Map integration, ClassLoader providedClassLoader) {
        if (persistenceUnitDescriptor instanceof ParsedPersistenceXmlDescriptor) {
            ParsedPersistenceXmlDescriptor descriptor = (ParsedPersistenceXmlDescriptor) persistenceUnitDescriptor;
            final Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("ch")))
                    .setUrls(ClasspathHelper.forPackage("ch")).setScanners(new TypeAnnotationsScanner()).
                    setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));

            for (final Class<? extends Annotation> annotationClass : entityAnnotationClasses) {
                for (final Class<?> annotatedClass : reflections.getTypesAnnotatedWith(annotationClass)) {
                    descriptor.addClasses(annotatedClass.getName());
                }
            }
        }
        return super.getEntityManagerFactoryBuilder(persistenceUnitDescriptor, integration, providedClassLoader);
    }

    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties) {
        return getEntityManagerFactoryBuilderOrNullOverride(persistenceUnitName, properties, null);
    }

    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties, ClassLoader providedClassLoader) {
        return getEntityManagerFactoryBuilderOrNullOverride(persistenceUnitName, properties, providedClassLoader);
    }

    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNullOverride(String persistenceUnitName, Map properties, ClassLoader providedClassLoader) {
        final Map<?, ?> integration = wrap(properties);
        final List<ParsedPersistenceXmlDescriptor> units;
        try {
            units = PersistenceXmlParser.locatePersistenceUnits(integration);
        } catch (Exception e) {
            throw new PersistenceException("Unable to locate persistence units", e);
        }

        if (persistenceUnitName == null && units.size() > 1) {
            throw new PersistenceException("No name provided and multiple persistence units found");
        }

        for (ParsedPersistenceXmlDescriptor persistenceUnit : units) {
            final boolean matches = persistenceUnitName == null || persistenceUnit.getName().equals(persistenceUnitName);
            if (!matches) {
                continue;
            }

            String extractRequestedProviderName = ProviderChecker.extractRequestedProviderName(persistenceUnit, integration);

            if (!ProviderChecker.isProvider(persistenceUnit, properties) && !(this.getClass().getName().equals(extractRequestedProviderName))) {
                continue;
            }

            return getEntityManagerFactoryBuilder(persistenceUnit, integration, providedClassLoader);
        }

        return null;
    }

}