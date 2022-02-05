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

package ch.platform.common.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class BaseDaoImpl<T> implements BaseDao<T> {

    public static EntityManager entityManagerOverride;

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    @Override
    public void persistObject(T object) {
        getEntityManager().persist(object);
    }

    @Override
    public void mergeObject(T object) {
        getEntityManager().merge(object);
    }

    @Override
    public Object findObject(Class<?> t, Object id) {
        return getEntityManager().find(t, id);
    }

    @Override
    public void removeObject(T object) {
        getEntityManager().remove(object);
    }

    @Override
    public void refreshObject(T object) {
        getEntityManager().refresh(object);
    }

    @Override
    public Object getReferenceObject(Class<?> t, Object id) {
        return getEntityManager().getReference(t, id);
    }

    @Override
    public boolean contains(Object id) {
        return getEntityManager().contains(id);
    }

    protected EntityManager getEntityManager() {
        return null == entityManagerOverride ? entityManager : entityManagerOverride;
    }

}