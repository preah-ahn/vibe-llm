package io.vibe.llm.basis.document;

import io.vibe.llm.common.engine.test.SuperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document test
 **********************************************************************************************************************/
@Transactional
public class DocumentTest extends SuperTest {

    @BeforeEach
    void setUp() {
        DocumentHelper.mock = mock;
    }

    @Test
    public void t01_getPage() {
        DocumentHelper.add(DocumentHelper.addDocument());
        DocumentHelper.add(DocumentHelper.addDocument());
        DocumentHelper.getPage(DocumentHelper.findDocument());
    }

    @Test
    public void t02_get() {
        DocumentHelper.get(DocumentHelper.add(DocumentHelper.addDocument()));
    }

    @Test
    public void t03_add() {
        DocumentHelper.add(DocumentHelper.addDocument());
    }

    @Test
    public void t04_modify() {
        DocumentHelper.modify(DocumentHelper.add(DocumentHelper.addDocument()), DocumentHelper.modifyDocument());
    }

    @Test
    public void t05_changeStatus() {
        DocumentHelper.changeStatus(DocumentHelper.add(DocumentHelper.addDocument()), DocumentHelper.changeStatusDocument());
    }

    @Test
    public void t06_remove() {
        DocumentHelper.remove(DocumentHelper.add(DocumentHelper.addDocument()));
    }
}
