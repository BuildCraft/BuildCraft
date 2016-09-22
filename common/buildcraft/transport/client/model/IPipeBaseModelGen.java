package buildcraft.transport.client.model;

import java.util.List;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.config.DetailedConfigOption;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;

public interface IPipeBaseModelGen {
    public static final DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.725");

    List<MutableQuad> generateCutout(PipeBaseCutoutKey key);

    List<MutableQuad> generateTranslucent(PipeBaseTransclucentKey key);
}
