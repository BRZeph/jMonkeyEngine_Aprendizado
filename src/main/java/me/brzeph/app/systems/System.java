package me.brzeph.app.systems;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.factory.EntityFactory;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmeAudio;
import me.brzeph.infra.jme.adapter.JmeRender;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

import java.util.concurrent.ConcurrentHashMap;

public abstract class System implements SystemAbs {
    private final Node root;
    private final EventBus bus;
    private final JmeAudio audio;
    private final JmeRender renderer;
    private final EntityPhysicsAdapter entityPhysicsAdapter;
    private final AssetManager assetManager;
    private final EntityFactory entityFactory;
    private final SimpleApplication app;

    private static final ConcurrentHashMap<Class<?>, System> systems = new ConcurrentHashMap<>();

    public System() {
        this.audio = ServiceLocator.get(JmeAudio.class);
        this.renderer = ServiceLocator.get(JmeRender.class);
        this.root = ServiceLocator.get(Node.class);
        this.bus = ServiceLocator.get(EventBus.class);
        this.entityPhysicsAdapter = ServiceLocator.get(EntityPhysicsAdapter.class);
        this.app = ServiceLocator.get(SimpleApplication.class);
        this.assetManager = ServiceLocator.get(AssetManager.class);
        this.entityFactory = new EntityFactory(assetManager, entityPhysicsAdapter);
        java.lang.System.out.println("GUI System");
        registerSystem(this);
        subscribe(); // TODO: utilizar SystemsWiring ao invés disso.
    }

    public static void cleanUp(){
        systems.clear();
    }

    public static void registerSystem(System system) {
        systems.put(system.getClass(), system);
    }

    public static System getSystem(Class<? extends System> clazz) {
        return systems.get(clazz);
    }

    public Node getRoot() {
        return root;
    }

    public EventBus getBus() {
        return bus;
    }

    public JmeAudio getAudio() {
        return audio;
    }

    public JmeRender getRenderer() {
        return renderer;
    }

    public EntityPhysicsAdapter getEntityPhysicsAdapter() {
        return entityPhysicsAdapter;
    }

    public SimpleApplication getApp() {
        return app;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }
}
/*
Game entity:

    protected Vector3f position;
    protected Quaternion rotation;
    protected Geometry geometry;
    protected Spatial spatial; // Ou o pai de Spatial.
 */