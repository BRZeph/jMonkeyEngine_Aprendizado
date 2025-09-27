package me.brzeph.app.systems;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.app.factory.EntityFactory;
import me.brzeph.events.EventBus;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SystemAbs implements SystemInt {
    private final Node root;
    private final BulletAppState bullet;
    private final EventBus bus;
    private final EntityPhysicsAdapter entityPhysicsAdapter;
    private final PhysicsSpace physicsSpace;
    private final AssetManager assetManager;
    private final EntityFactory entityFactory;
    private final SimpleApplication app;

    private static final ConcurrentHashMap<Class<?>, SystemAbs> systems = new ConcurrentHashMap<>();

    public SystemAbs() {
        this.root = ServiceLocator.get(Node.class);
        this.bullet = ServiceLocator.get(BulletAppState.class);
        this.bus = ServiceLocator.get(EventBus.class);
        this.entityPhysicsAdapter = ServiceLocator.get(EntityPhysicsAdapter.class);
        this.physicsSpace = ServiceLocator.get(PhysicsSpace.class);
        this.app = ServiceLocator.get(SimpleApplication.class);
        this.assetManager = ServiceLocator.get(AssetManager.class);
        this.entityFactory = ServiceLocator.get(EntityFactory.class);
        registerSystem(this);
    }

    public static void cleanUp(){
        systems.clear();
    }

    private static void registerSystem(SystemAbs systemAbs) {
        systems.put(systemAbs.getClass(), systemAbs);
    }

    public static <T extends SystemAbs> T getSystem(Class<T> clazz) {
        return clazz.cast(systems.get(clazz));
    }

    public Node getRoot() {
        return root;
    }

    public BulletAppState getBullet() {
        return bullet;
    }

    public EventBus getBus() {
        return bus;
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

    public PhysicsSpace getPhysicsSpace() {
        return physicsSpace;
    }
}