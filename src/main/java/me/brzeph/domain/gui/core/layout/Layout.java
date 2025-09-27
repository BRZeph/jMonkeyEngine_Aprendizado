package me.brzeph.domain.gui.core.layout;

import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;
import me.brzeph.domain.gui.core.widgets.Panel;

public interface Layout {
    /** Mede o tamanho preferido do PARENT (ex.: Panel) dado um espaço máximo. */
    Size measure(Panel parent, float maxW, float maxH);

    /** Aplica o layout aos FILHOS do parent dentro de 'bounds'. */
    void apply(Panel parent, Rect bounds);
}

