package com.force.bookstore;

import com.force.bookstore.model.Author;
import com.force.bookstore.service.BookstoreService;
import com.force.bookstore.service.Persistable;
import com.force.sdk.connector.ForceServiceConnector;
import org.junit.After;

import javax.inject.Inject;
import javax.jdo.annotations.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertTrue;

/**
 * @author rbrainard
 */
public abstract class PersistableBaseTest {
    public static final String AUTHOR_FIRST_NAME = "John";
    public static final String AUTHOR_LAST_NAME = "Steinbeck";
    public static final String TITLE_1 = "Of Mice and Men";
    public static final String TITLE_2 = "The Grapes of Wrath";
    private Set<Persistable> createdEntities = new HashSet<Persistable>();
    @Inject
    protected BookstoreService bookstore;
    @Inject
    private ForceServiceConnector connector;
    @PersistenceContext
    protected EntityManager em;

    @After
    public void tearDown() throws Exception {
        Set<String> ids = new HashSet<String>();
        for (Persistable p : createdEntities) {
            if (null != p.getId()) {
                ids.add(p.getId());
            }
        }

        connector.getConnection().delete(ids.toArray(new String[ids.size()]));
    }

    protected <E extends Persistable> E register(E e) {
        createdEntities.add(e);
        return e;
    }

    @Transactional
    protected void txFindAndAssert(Author author) {
        final Author directlyLoadedAuthor = txFind(Author.class, author.getId());
        assertTrue(directlyLoadedAuthor.wasPostLoadHookCalled());
        assertTrue(directlyLoadedAuthor.getBooks().iterator().next().wasPostLoadHookCalled());
    }

    @Transactional
    public <T> T txFind(Class<T> entityClass, Object primaryKey) {
        return em.find(entityClass, primaryKey);
    }

    @Transactional
    public <V> V txCall(Callable<V> callable) throws Exception {
        return callable.call();
    }
}
