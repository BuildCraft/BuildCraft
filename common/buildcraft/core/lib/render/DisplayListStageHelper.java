package buildcraft.core.lib.render;

import buildcraft.core.lib.render.DisplayListHelper.IGenerator;

public class DisplayListStageHelper {
    public interface IStageGenerator {
        /** @param stage A float between 0 (inclusive) and 1 (inclusive) */
        void generate(float stage);
    }

    public static final class DisplayListGenerator implements IGenerator {
        final float stage;
        final IStageGenerator gen;

        public DisplayListGenerator(int stage, int numStages, IStageGenerator gen) {
            this.stage = stage / (float) (numStages - 1);
            this.gen = gen;
        }

        @Override
        public void generate() {
            gen.generate(stage);
        }
    }

    private final DisplayListHelper[] stages;

    public DisplayListStageHelper(int numStages, IStageGenerator gen) {
        stages = new DisplayListHelper[numStages];
        for (int i = 0; i < numStages; i++) {
            stages[i] = new DisplayListHelper(new DisplayListGenerator(i, numStages, gen));
        }
    }

    public void renderStage(float stage) {
        if (stage < 0) {
            stage = 0;
        } else if (stage > 1) {
            stage = 1;
        }

        int index = (int) (stage * (stages.length - 1));

        stages[index].render();
    }

    public void delete() {
        for (DisplayListHelper vboHelper : stages) {
            vboHelper.delete();
        }
    }
}
