package me.brzeph.app.systems.impl.animationSystem;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

public class AnimationService {
    public static <T extends Control> T findControl(Spatial s, Class<T> type) {
        if (s == null) return null;
        T c = s.getControl(type);
        if (c != null) return c;
        if (s instanceof Node n) {
            for (Spatial ch : n.getChildren()) {
                T f = findControl(ch, type);
                if (f != null) return f;
            }
        }
        return null;
    }

    public static void dumpScene(Spatial s, String indent) {
        System.out.println(indent + "- " + s.getName() + " [" + s.getClass().getSimpleName() + "]");
        for (int i = 0; i < s.getNumControls(); i++) {
            System.out.println(indent + "  * Control: " + s.getControl(i).getClass().getName());
        }
        if (s instanceof Node n) for (Spatial ch : n.getChildren()) dumpScene(ch, indent + "  ");
    }
}
