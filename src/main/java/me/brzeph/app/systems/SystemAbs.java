package me.brzeph.app.systems;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.factory.EntityFactory;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmeAudio;
import me.brzeph.infra.jme.adapter.JmeRender;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SystemAbs implements SystemInt {
    private final Node root;
    private final BulletAppState bullet;
    private final EventBus bus;
    private final JmeAudio audio;
    private final JmeRender renderer;
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
        this.audio = ServiceLocator.get(JmeAudio.class);
        this.renderer = ServiceLocator.get(JmeRender.class);
        this.entityPhysicsAdapter = ServiceLocator.get(EntityPhysicsAdapter.class);
        this.physicsSpace = ServiceLocator.get(PhysicsSpace.class);
        this.app = ServiceLocator.get(SimpleApplication.class);
        this.assetManager = ServiceLocator.get(AssetManager.class);
        this.entityFactory = new EntityFactory(assetManager, physicsSpace);
        registerSystem(this);
    }

    public static void cleanUp(){
        systems.clear();
    }

    private static void registerSystem(SystemAbs systemAbs) {
        systems.put(systemAbs.getClass(), systemAbs);
    }

    public static SystemAbs getSystem(Class<? extends SystemAbs> clazz) {
        return systems.get(clazz);
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

    public PhysicsSpace getPhysicsSpace() {
        return physicsSpace;
    }
}