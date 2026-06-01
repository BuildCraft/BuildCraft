// STUB(R.Chen): Forge RenderGameOverlayEvent — compile shim.
package net.minecraftforge.client.event;

public class RenderGameOverlayEvent {
    public enum ElementType { ALL, CROSSHAIRS, HOTBAR, HEALTH, ARMOR, FOOD, HEALTHMOUNT, JUMPBAR, EXPERIENCE, TEXT, POTION_ICONS, BOSSHEALTH, AIR, HELMET, FPSCOUNT, DEBUG, CHAT, PLAYER_LIST, SUBTITLES }

    private final ElementType type;
    private float partialTicks;

    public RenderGameOverlayEvent(float partialTicks, ElementType type) {
        this.partialTicks = partialTicks;
        this.type = type;
    }

    public ElementType getType() { return type; }
    public float getPartialTicks() { return partialTicks; }

    public static class Pre extends RenderGameOverlayEvent {
        public Pre(float partialTicks, ElementType type) { super(partialTicks, type); }
    }

    public static class Post extends RenderGameOverlayEvent {
        public Post(float partialTicks, ElementType type) { super(partialTicks, type); }
    }
}
