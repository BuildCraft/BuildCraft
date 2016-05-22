package buildcraft.lib.client.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;

import net.minecraft.util.ResourceLocation;

public class StringResourceHolder extends ResourceHolder {
    private static final String REGEX_LINE_END = "\\R";
    private List<String> lines = new ArrayList<>();

    public StringResourceHolder(ResourceLocation location) {
        super(location);
    }

    public List<String> getLines() {
        return lines;
    }

    @Override
    protected final void onLoad(byte[] data) {
        String fullData = new String(data, Charsets.UTF_8);
        List<String> newLines = new ArrayList<>();
        for (String s : fullData.split(REGEX_LINE_END)) {
            newLines.add(s);
        }
        lines = newLines;
        onStringChange();
    }

    public void onStringChange() {}
}
