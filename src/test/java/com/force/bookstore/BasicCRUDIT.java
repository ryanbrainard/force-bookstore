package com.force.bookstore;

import com.force.bookstore.model.MyEntity;
import com.force.bookstore.service.MyEntityService;
import com.force.sdk.connector.ForceServiceConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/app-context.xml")
public class BasicCRUDIT {

    @Inject
//    private AbstractEntityService<MyEntity> entityService;
    private MyEntityService entityService;

    @Inject
    private ForceServiceConnector connector;

    @Test
    public void testEntityCRUD() throws Exception {

        String entityId = null;

        try {
            MyEntity entity = new MyEntity();
            entity.setName("A Test Entity");

            entityService.save(entity);
            assertNotNull(entity.getId());

            entityId = entity.getId();
            entity = entityService.findEntity(entityId);
            assertEquals("A Test Entity", entity.getName());

            entity.setName("A Modified Test Entity");
            entityService.save(entity);
            entity = entityService.findEntity(entityId);
            assertEquals("A Modified Test Entity", entity.getName());

            entityService.delete(entityId);
            entity = entityService.findEntity(entityId);
            assertNull(entity);
            entityId = null;
        } finally {
            if (entityId != null) {
                connector.getConnection().delete(new String[]{entityId});
            }
        }
    }
}
