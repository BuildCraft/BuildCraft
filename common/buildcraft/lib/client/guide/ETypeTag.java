package buildcraft.lib.client.guide;

public enum ETypeTag {
    MOD("mod."),
    SUB_MOD("submod."),
    TYPE("type."),
    SUB_TYPE("subtype.");

    public final String preText;

    ETypeTag(String preText) {
        this.preText = "buildcraft.guide.chapter." + preText;
    }
}