package me.brzeph.app.systems.impl;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import me.brzeph.app.systems.System;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.app.systems.impl.PlayerSystem;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

import static me.brzeph.infra.constants.EnemiesConstants.EPS;

public class CameraSystem extends System {
    private final Camera cam;
    private final Spatial playerNode;
    private ChaseCamera chase;
    private Vector3f lookAtOffset = new Vector3f(0, 1.6f, 0); // já usa algo assim
    private float baseDistance = 10f;
    private float minDistance  = 1.5f;   // quão perto pode chegar do player
    private float maxDistance  = 20f;
    private float camRadius    = 0.25f;  // “raio” visual da câmera (margem contra clipping)

    public CameraSystem() {
        this.playerNode = ((PlayerSystem)getSystem(PlayerSystem.class)).getPlayerSpatial();
        this.cam = initCamera();
        ((PlayerSystem)getSystem(PlayerSystem.class)).setCam(cam);
    }

    @Override
    public void subscribe() {

    }

    private Camera initCamera() {
        Camera cam = getApp().getCamera();
        ServiceLocator.put(Camera.class, cam);

        cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);

        getApp().getFlyByCamera().setEnabled(false);

        chase = new ChaseCamera(cam, playerNode, getApp().getInputManager());
        chase.setDefaultDistance(baseDistance);
        chase.setMaxDistance(maxDistance);
        chase.setMinDistance(minDistance);
        chase.setLookAtOffset(lookAtOffset);
        chase.setRotationSpeed(2f);
        chase.setDragToRotate(false);
        chase.setUpVector(Vector3f.UNIT_Y);
        chase.setMinVerticalRotation(-FastMath.HALF_PI + 0.01f);
        chase.setMaxVerticalRotation( FastMath.HALF_PI - 0.01f);

        return cam;
    }

    @Override
    public void update(float tpf) {
        // zera roll (mantém câmera paralela ao chão)
        Quaternion q = new Quaternion();
        q.lookAt(getApp().getCamera().getDirection(), Vector3f.UNIT_Y);
        getApp().getCamera().setRotation(q);

        // evita que a câmera entre no terreno/parede
        preventCameraClipping();
    }

    public Camera getCam() {
        return cam;
    }

    private void preventCameraClipping() { // TODO: isso deveria estar dentro de CameraService.
        Camera cam = getApp().getCamera();
        Vector3f pivot = playerNode.getWorldTranslation().add(lookAtOffset);

        // direção desejada: do pivot para a câmera atual (mantém yaw/pitch do usuário)
        Vector3f toCam = cam.getLocation().subtract(pivot);
        float curDist = toCam.length();
        if (curDist < EPS) toCam = cam.getDirection().negate(); else toCam.divideLocal(curDist); // normalize

        float desired = baseDistance; // distância-alvo sem obstáculos
        desired = FastMath.clamp(desired, minDistance, maxDistance);
        Vector3f desiredPos = pivot.add(toCam.mult(desired));

        // Raycast do pivot até a posição desejada
        EntityPhysicsAdapter phys = ServiceLocator.get(EntityPhysicsAdapter.class);
        var hits = phys.rayTest(pivot, desiredPos);

        float allowed = desired;
        if (hits != null && !hits.isEmpty()) {
            // pega o hit mais próximo que NÃO seja o próprio player
            hits.sort(java.util.Comparator.comparingDouble(com.jme3.bullet.collision.PhysicsRayTestResult::getHitFraction));
            for (var r : hits) {
                Object uo = r.getCollisionObject().getUserObject();
                if (isSelf(uo)) continue;           // ignora corpo do player
                float f = (float) r.getHitFraction();
                float hitDist = desired * f;
                allowed = Math.min(allowed, Math.max(minDistance, hitDist - camRadius)); // encurta com margem
                break;
            }
        }

        allowed = FastMath.clamp(allowed, minDistance, maxDistance);

        // Posiciona a câmera na distância permitida
        Vector3f safePos = pivot.add(toCam.mult(allowed));
        cam.setLocation(safePos);

        // dizer ao ChaseCamera a “distância atual” ajuda a evitar jitter
        chase.setDefaultDistance(allowed);
    }

    private boolean isSelf(Object uo) {
        if (uo == null) return false;
        if (uo == playerNode) return true;
        if (uo instanceof Spatial s) {
            // se você marcou characterId no Spatial:
            String a = s.getUserData("characterId");
            String b = playerNode.getUserData("characterId");
            return a != null && a.equals(b);
        }
        return false;
    }
}

