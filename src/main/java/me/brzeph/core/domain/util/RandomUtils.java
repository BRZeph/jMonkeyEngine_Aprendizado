package me.brzeph.core.domain.util;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import com.jme3.math.Vector3f;
import java.util.Random;

public class RandomUtils {

    private static final Random RNG = new Random();

    /**
     * Retorna uma posição aleatória ao redor de `position` com distância máxima `maxDst`.
     * A posição é gerada no plano XZ (Y mantém o valor original).
     */
    public static Vector3f getRandomIdlePosition(Vector3f position, float maxDst) {
        float random = RNG.nextFloat();
        if(random < 0.5f) random += 0.5f;
        float distance = random * maxDst;

        float angle = RNG.nextFloat() * FastMath.TWO_PI;

        float dx = distance * FastMath.cos(angle);
        float dz = distance * FastMath.sin(angle);

        return position.add(dx, 0, dz);
    }
}

