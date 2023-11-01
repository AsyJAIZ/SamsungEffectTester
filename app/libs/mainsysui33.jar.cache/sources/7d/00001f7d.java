package com.android.systemui.plugins;

import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = GlobalActions.ACTION, version = 1)
@DependsOn(target = GlobalActionsManager.class)
/* loaded from: mainsysui33.jar:com/android/systemui/plugins/GlobalActions.class */
public interface GlobalActions extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_GLOBAL_ACTIONS";
    public static final int VERSION = 1;

    @ProvidesInterface(version = 1)
    /* loaded from: mainsysui33.jar:com/android/systemui/plugins/GlobalActions$GlobalActionsManager.class */
    public interface GlobalActionsManager {
        public static final int VERSION = 1;

        void onGlobalActionsHidden();

        void onGlobalActionsShown();

        void reboot(boolean z, String str);

        void shutdown();
    }

    default void destroy() {
    }

    void showGlobalActions(GlobalActionsManager globalActionsManager);

    default void showShutdownUi(boolean z, String str, boolean z2) {
    }
}