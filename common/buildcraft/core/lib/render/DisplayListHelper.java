package buildcraft.core.lib.render;

import org.lwjgl.opengl.GL11;

public class DisplayListHelper {
    public interface IGenerator {
        public void generate();
    }

    private final IGenerator gen;

    public DisplayListHelper(IGenerator gen) {
        this.gen = gen;
    }

    private int id = 0;

    public void render() {
        if (id == 0) {
            create();
        }
        fireRender();
    }

    private void create() {
        id = GL11.glGenLists(1);
        GL11.glNewList(id, GL11.GL_COMPILE);
        gen.generate();
        GL11.glEndList();
    }

    private void fireRender() {
        GL11.glCallList(id);
    }

    public void delete() {
        if (id != 0) {
            GL11.glDeleteLists(id, 1);
            id = 0;
        }
    }
}
