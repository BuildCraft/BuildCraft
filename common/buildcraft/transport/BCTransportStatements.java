/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport;

import buildcraft.api.statements.ITriggerInternal;

// STUB(R.Chen): full implementation in Phase 4E.
// The full statement registry depends on ~15 unmigrated Trigger*/Action* classes
// (TriggerPipeSignal, ActionPipeColor, ActionPowerLimit, …), ColourUtil, PipeBehaviourLimiter,
// PipeBehaviourEmzuli.SlotIndex, BCTransportConfig and StatementManager registration. Only the
// TRIGGER_ITEMS_TRAVERSING / TRIGGER_FLUIDS_TRAVERSING fields referenced by
// PipeFlowItems#addTriggers / PipeFlowFluids#addTriggers are exposed; they are null until the
// statement layer is migrated.
public class BCTransportStatements {

    public static final ITriggerInternal TRIGGER_ITEMS_TRAVERSING = null;
    public static final ITriggerInternal TRIGGER_FLUIDS_TRAVERSING = null;

    public static void preInit() {
        // STUB(R.Chen): trigger/action provider registration deferred to Phase 4E.
    }
}
