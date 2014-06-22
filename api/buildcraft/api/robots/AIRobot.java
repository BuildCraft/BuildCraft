
package buildcraft.api.robots;

import buildcraft.api.core.SafeTimeTracker;

public class AIRobot {
	public EntityRobotBase robot;

	private AIRobot delegateAI;
	private AIRobot parentAI;
	private double energyCost;
	private SafeTimeTracker updateTracker;

	public AIRobot(EntityRobotBase iRobot, double iEnergyCost, int updateLatency) {
		robot = iRobot;
		energyCost = iEnergyCost;

		if (updateLatency > 1) {
			updateTracker = new SafeTimeTracker(updateLatency, updateLatency / 5);
		}
	}

	public void start() {

	}

	public void preempt(AIRobot ai) {

	}

	public void update() {

	}

	public void end() {

	}

	public void delegateAIEnded(AIRobot ai) {

	}

	public final void terminate() {
		abortDelegateAI();
		end();

		if (parentAI != null) {
			parentAI.delegateAI = null;
			parentAI.delegateAIEnded(this);
		}
	}

	public final void cycle() {
		preempt(delegateAI);

		if (delegateAI != null) {
			delegateAI.cycle();
		} else {
			if (updateTracker == null || updateTracker.markTimeIfDelay(robot.worldObj)) {
				robot.setEnergy(robot.getEnergy() - energyCost);
				update();
			}
		}
	}

	public final void startDelegateAI(AIRobot ai) {
		abortDelegateAI();
		delegateAI = ai;
		ai.parentAI = this;
		delegateAI.start();
	}

	public final void abortDelegateAI() {
		if (delegateAI != null) {
			delegateAI.terminate();
		}
	}
}
