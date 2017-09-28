package buildcraft.lib.gui.config;

@FunctionalInterface
public interface GuiPropertyConstructor {
    GuiProperty create(String name);
}
