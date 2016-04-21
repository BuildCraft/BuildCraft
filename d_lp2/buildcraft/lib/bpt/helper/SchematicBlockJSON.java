package buildcraft.lib.bpt.helper;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.SchematicBlock;

public class SchematicBlockJSON extends SchematicBlock {

    @Override
    public void rotate(Rotation rotation) {
        
    }

    @Override
    public void mirror(Mirror mirror) {
        
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilder builder) {
        return null;
    }
}
