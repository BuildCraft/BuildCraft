package buildcraft.lib.guide;

import buildcraft.api.BCModules;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.ModUtil;

import javax.annotation.Nullable;
import java.util.*;

public class GuideContentsData {

    public final @Nullable GuideBook book;

    public final List<String> loadedMods = new ArrayList<>();
    public final List<String> loadedOther = new ArrayList<>();

    public GuideContentsData(@Nullable GuideBook book) {
        this.book = book;
    }

    public void generate(Set<String> domains) {
        loadedMods.clear();
        loadedOther.clear();
        Set<BCModules> bcmods = EnumSet.noneOf(BCModules.class);
        for (String domain : domains) {
            if (domain == null) {
                throw new IllegalArgumentException("Was given a null domain!");
            }
            BCModules bcMod = BCModules.getBcMod(domain);
            if (bcMod != null) {
                bcmods.add(bcMod);
            } else {
                String name = ModUtil.getNameOfMod(domain);
                if (name != null) {
                    loadedMods.add(name);
                } else {
                    loadedOther.add(LocaleUtil.localize(domain + ".compat.buildcraft.guide.domain_name"));
                }
            }
        }
        Collections.sort(loadedMods);
        Collections.sort(loadedOther);
        switch (bcmods.size()) {
            case 0:
                return;
            case 6: {
                if (!loadedMods.contains(BCModules.COMPAT.getModId())) {
                    loadedMods.add(0, "BuildCraft (main)");
                    return;
                }
                break;
            }
            case 7: {
                loadedMods.add(0, "BuildCraft (all)");
                return;
            }
            default: {
                break;
            }
        }
        List<String> bcModNames = new ArrayList<>(6);
        if (bcmods.remove(BCModules.LIB)) {
            bcModNames.add("BuildCraft Lib");
        }
        if (bcmods.remove(BCModules.CORE)) {
            bcModNames.add("BuildCraft Core");
        }
        for (BCModules mod : bcmods) {
            bcModNames.add("BuildCraft " + mod.camelCaseName);
        }
        loadedMods.addAll(0, bcModNames);
    }
}
