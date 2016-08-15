package buildcraft.lib.bpt.helper;

import java.util.ArrayList;
import java.util.List;

import buildcraft.api.bpt.IMaterialProvider.IRequested;

public class RequestedHelper implements IRequested {
    private final List<IRequested> requested = new ArrayList<>();
    private boolean locked = false, used = false;

    public void and(IRequested request) {
        requested.add(request);
    }

    @Override
    public boolean lock() throws IllegalStateException {
        if (used) {
            throw new IllegalArgumentException("Already used!");
        }
        boolean all = true;
        for (IRequested req : requested) {
            all &= req.lock();
        }
        locked = all;
        return locked;
    }

    @Override
    public boolean isLocked() {
        return locked || used;
    }

    @Override
    public void use() throws IllegalStateException {
        if (used) {
            throw new IllegalStateException("Already used!");
        }
        if (!locked) {
            throw new IllegalStateException("Not locked!");
        }
        locked = false;
        used = true;

        for (IRequested req : requested) {
            req.use();
        }
    }

    @Override
    public void release() {
        for (IRequested req : requested) {
            req.release();
        }
        locked = false;
        used = true;
    }
}
