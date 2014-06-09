
package buildcraft.robots;

public class AIRobot {
	public EntityRobotBase robot;

	private AIRobot delegateAI;
	private AIRobot parentAI;

	public AIRobot(EntityRobotBase iRobot) {
		robot = iRobot;
	}

	public void start() {

	}

	public void preempt() {

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
		preempt();

		if (delegateAI != null) {
			delegateAI.cycle();
		} else {
			update();
		}
	}

	public final void startDelegateAI(AIRobot ai) {
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
