package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;


public class AutoLogin extends Module {

    public AutoLogin() {
        super("AutoLogin", KEY_UNBOUND, ModuleCategory.MISC, "Automatically logs into servers.");
    }

  @BleachSubscribe
    public void onTick(EventTick event) {

    }
}