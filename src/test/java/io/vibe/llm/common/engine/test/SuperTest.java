package io.vibe.llm.common.engine.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @since       2026.10.01
 * @author      preah
 * @description super test
 **********************************************************************************************************************/
@SpringBootTest
@AutoConfigureMockMvc
public abstract class SuperTest {

    @Autowired
    public MockMvc mock;
}
