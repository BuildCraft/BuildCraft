
package buildcraft.api.robots;

public class AIRobot {
	public EntityRobotBase robot;

	private AIRobot delegateAI;
	private AIRobot parentAI;
	private double energyCost;

	public AIRobot(EntityRobotBase iRobot, double iEnergyCost) {
		robot = iRobot;
		energyCost = iEnergyCost;
	}

	public AIRobot(EntityRobotBase iRobot) {
		robot = iRobot;
		energyCost = 0;
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
			robot.setEnergy(robot.getEnergy() - energyCost);
			update();
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
			delegateAI = null;
		}
	}
}
