package org.lantern.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.apache.commons.lang.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lantern.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelIoTest {

    private static Logger LOG = LoggerFactory.getLogger(ModelIoTest.class);

    private static File testFile;

    @BeforeClass
    public static void setup() throws Exception {
        testFile = new File("modelTest");
        testFile.delete();
        testFile.deleteOnExit();
    }
    
    @Test 
    public void testMapperFactory() throws Exception {
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.registerClassMap(factory.classMap(Model.class,Model.class).byDefault().toClassMap());
        final MapperFacade mapper = factory.getMapperFacade();
        final Model blankModel = new Model();
        final Model mod = TestUtils.getModel();
        assertFalse(mod.isLaunchd());
        
        mod.setLaunchd(true);
        mapper.map(blankModel, mod);
        assertFalse(mod.isLaunchd());
    }
    
    @Test
    public void testModelIo() throws Exception {
        ModelIo io = 
            new ModelIo(testFile, TestUtils.getEncryptedFileService());
        
        Model model = io.get();
        
        final String id = model.getNodeId();
        SystemData system = model.getSystem();
        Settings settings = model.getSettings();
        Connectivity connectivity = model.getConnectivity();
        assertEquals("", connectivity.getIp());
        
        final String ip = "30.2.2.2";
        //connectivity.setIp(ip);
        
        assertEquals(0, model.getNinvites());
        model.setNinvites(10);
        assertEquals(10, model.getNinvites());
        
        assertEquals(true, settings.isAutoStart());
        settings.setAutoStart(false);
        
        if ("en".equalsIgnoreCase(SystemUtils.USER_LANGUAGE)) {
            assertEquals("en", system.getLang());
        }
        io.write();
        
        io = new ModelIo(testFile, TestUtils.getEncryptedFileService());
        model = io.get();
        system = model.getSystem();
        settings = model.getSettings();
        connectivity = model.getConnectivity();
        assertEquals(false, settings.isAutoStart());
        assertEquals(10, model.getNinvites());
        
        // The user's IP address should not persist to disk
        assertEquals("", connectivity.getIp());
        
        assertEquals("ID should persist across sessions", 
            id, model.getNodeId());
        
    }

}
